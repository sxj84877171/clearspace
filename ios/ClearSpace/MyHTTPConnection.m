
#import "MyHTTPConnection.h"
#import "HTTPMessage.h"
#import "HTTPDataResponse.h"
#import "DDNumber.h"
#import "HTTPLogging.h"

#import "MultipartFormDataParser.h"
#import "MultipartMessageHeaderField.h"
#import "HTTPDynamicFileResponse.h"
#import "HTTPFileResponse.h"
#import "HTTPDataResponse.h"
#import "HTTPErrorResponse.h"
#import "HTTPAsyncVideoResponse.h"

#import "PublicData.h"
#import "CSConst.h"
#import "AssetsManager.h"

// Log levels : off, error, warn, info, verbose
// Other flags: trace
static const int httpLogLevel = HTTP_LOG_LEVEL_VERBOSE; // | HTTP_LOG_FLAG_TRACE;


/**
 * All we have to do is override appropriate methods in HTTPConnection.
 **/

@implementation MyHTTPConnection

- (BOOL)supportsMethod:(NSString *)method atPath:(NSString *)path
{
	HTTPLogTrace();
	
	// Add support for POST
	
	if ([method isEqualToString:@"POST"])
	{
		if ([path isEqualToString:@"/upload.html"])
		{
			return YES;
		}
	}
	
	return [super supportsMethod:method atPath:path];
}

- (BOOL)expectsRequestBodyFromMethod:(NSString *)method atPath:(NSString *)path
{
	HTTPLogTrace();
	
	// Inform HTTP server that we expect a body to accompany a POST request
	
	if([method isEqualToString:@"POST"] && [path isEqualToString:@"/upload.html"]) {
        // here we need to make sure, boundary is set in header
        NSString* contentType = [request headerField:@"Content-Type"];
        NSUInteger paramsSeparator = [contentType rangeOfString:@";"].location;
        if( NSNotFound == paramsSeparator ) {
            return NO;
        }
        if( paramsSeparator >= contentType.length - 1 ) {
            return NO;
        }
        NSString* type = [contentType substringToIndex:paramsSeparator];
        if( ![type isEqualToString:@"multipart/form-data"] ) {
            // we expect multipart/form-data content type
            return NO;
        }

		// enumerate all params in content-type, and find boundary there
        NSArray* params = [[contentType substringFromIndex:paramsSeparator + 1] componentsSeparatedByString:@";"];
        for( NSString* param in params ) {
            paramsSeparator = [param rangeOfString:@"="].location;
            if( (NSNotFound == paramsSeparator) || paramsSeparator >= param.length - 1 ) {
                continue;
            }
            NSString* paramName = [param substringWithRange:NSMakeRange(1, paramsSeparator-1)];
            NSString* paramValue = [param substringFromIndex:paramsSeparator+1];
            
            if( [paramName isEqualToString: @"boundary"] ) {
                // let's separate the boundary from content-type, to make it more handy to handle
                [request setHeaderField:@"boundary" value:paramValue];
            }
        }
        // check if boundary specified
        if( nil == [request headerField:@"boundary"] )  {
            return NO;
        }
        return YES;
    }
	return [super expectsRequestBodyFromMethod:method atPath:path];
}

- (NSObject<HTTPResponse> *)httpResponseForMethod:(NSString *)method URI:(NSString *)path
{
	HTTPLogTrace();
	
	if ([method isEqualToString:@"POST"] && [path isEqualToString:@"/upload.html"])
	{

		// this method will generate response with links to uploaded file
		NSMutableString* filesStr = [[NSMutableString alloc] init];

		for( NSString* filePath in uploadedFiles ) {
			//generate links
			[filesStr appendFormat:@"<a href=\"%@\"> %@ </a><br/>",filePath, [filePath lastPathComponent]];
		}
		NSString* templatePath = [[config documentRoot] stringByAppendingPathComponent:@"upload.html"];
		NSDictionary* replacementDict = [NSDictionary dictionaryWithObject:filesStr forKey:@"MyFiles"];
		// use dynamic file response to apply our links to response template
		return [[HTTPDynamicFileResponse alloc] initWithFilePath:templatePath forConnection:self separator:@"%" replacementDictionary:replacementDict];
	}
	if( [method isEqualToString:@"GET"]) {
        return [self processDownloadRequest:path];
    }
	
	return [super httpResponseForMethod:method URI:path];
}

- (void)prepareForBodyWithSize:(UInt64)contentLength
{
	HTTPLogTrace();
	
	// set up mime parser
    NSString* boundary = [request headerField:@"boundary"];
    parser = [[MultipartFormDataParser alloc] initWithBoundary:boundary formEncoding:NSUTF8StringEncoding];
    parser.delegate = self;

	uploadedFiles = [[NSMutableArray alloc] init];
}

- (void)processBodyData:(NSData *)postDataChunk
{
	HTTPLogTrace();
    // append data to the parser. It will invoke callbacks to let us handle
    // parsed data.
    [parser appendData:postDataChunk];
}


//-----------------------------------------------------------------
#pragma mark multipart form data parser delegate


- (void) processStartOfPartWithHeader:(MultipartMessageHeader*) header {
	// in this sample, we are not interested in parts, other then file parts.
	// check content disposition to find out filename

    MultipartMessageHeaderField* disposition = [header.fields objectForKey:@"Content-Disposition"];
	NSString* filename = [[disposition.params objectForKey:@"filename"] lastPathComponent];

    if ( (nil == filename) || [filename isEqualToString: @""] ) {
        // it's either not a file part, or
		// an empty form sent. we won't handle it.
		return;
	}    
	NSString* uploadDirPath = [[config documentRoot] stringByAppendingPathComponent:@"upload"];

	BOOL isDir = YES;
	if (![[NSFileManager defaultManager]fileExistsAtPath:uploadDirPath isDirectory:&isDir ]) {
		[[NSFileManager defaultManager]createDirectoryAtPath:uploadDirPath withIntermediateDirectories:YES attributes:nil error:nil];
	}
	
    NSString* filePath = [uploadDirPath stringByAppendingPathComponent: filename];
    if( [[NSFileManager defaultManager] fileExistsAtPath:filePath] ) {
        storeFile = nil;
    }
    else {
		HTTPLogVerbose(@"Saving file to %@", filePath);
		if(![[NSFileManager defaultManager] createDirectoryAtPath:uploadDirPath withIntermediateDirectories:true attributes:nil error:nil]) {
			HTTPLogError(@"Could not create directory at path: %@", filePath);
		}
		if(![[NSFileManager defaultManager] createFileAtPath:filePath contents:nil attributes:nil]) {
			HTTPLogError(@"Could not create file at path: %@ %s", filePath, strerror(errno));
		}
		storeFile = [NSFileHandle fileHandleForWritingAtPath:filePath];
		[uploadedFiles addObject: [NSString stringWithFormat:@"/upload/%@", filename]];
    }
}


- (void) processContent:(NSData*) data WithHeader:(MultipartMessageHeader*) header 
{
	// here we just write the output from parser to the file.
	if( storeFile ) {
		[storeFile writeData:data];
	}
}

- (void) processEndOfPartWithHeader:(MultipartMessageHeader*) header
{
	// as the file part is over, we close the file.
	[storeFile closeFile];
	storeFile = nil;
}

- (void) processPreambleData:(NSData*) data 
{
    // if we are interested in preamble data, we could process it here.

}

- (void) processEpilogueData:(NSData*) data 
{
    // if we are interested in epilogue data, we could process it here.

}

-(NSObject<HTTPResponse> *) processDownloadRequest:(NSString *)path{
    if([path hasPrefix:@"/download/"]){
        // let download the uploaded files
        NSRange range = [path rangeOfString:@"download" options:NSCaseInsensitiveSearch];
        NSString *filePath = [path substringFromIndex:range.location + range.length];
        PublicData *publicData = [PublicData sharedInstance];
        if (publicData) {
            if([publicData.downloadFileListPath isEqualToString:filePath]){
                publicData.startDownLoad = YES;
                publicData.startDownloadDate = [NSDate date];
                [[NSNotificationCenter defaultCenter] postNotificationName:NTF_ID_FILE_DOWNLAOD_START object: nil userInfo:nil];
                return [[HTTPFileResponse alloc] initWithFilePath: filePath forConnection:self];
            }
            else{
                if(!publicData.startDownLoad){
                    //iOS在传输过程中，杀掉app, 但pc端还在不断请求，当app重新启动时，收到pc的http请求返回空，告诉pc重新开始下载
                    NSString *string = @"";
                    return [[HTTPDataResponse alloc] initWithData:[string dataUsingEncoding:NSUTF8StringEncoding]];
                }
                
                AssetsManager *assetsManager = [AssetsManager sharedInstance];
                if(assetsManager){
                    AssetInfo *info = [assetsManager assetWithPath:filePath];
                    if(info){
                        [[NSNotificationCenter defaultCenter] postNotificationName:NTF_ID_FILE_DOWNLAOD_NEXT object: nil userInfo:@{@"file":filePath}];
                        
                        if(info.mediaType == PHAssetMediaTypeImage){
                            PHImageRequestOptions *option = [[PHImageRequestOptions alloc] init];
                            option.synchronous = YES;
                            __block NSData *data = nil;
                            [[PHImageManager defaultManager] requestImageDataForAsset:info.asset options:option resultHandler:^(NSData * _Nullable imageData, NSString * _Nullable dataUTI, UIImageOrientation orientation, NSDictionary * _Nullable info) {
                                data = imageData;
                            }];
                            
                            return [[HTTPDataResponse alloc] initWithData:data];
                        }
                        else if(info.mediaType == PHAssetMediaTypeVideo){
                            return [[HTTPAsyncVideoResponse alloc] initWithFileHandle:info.fileHandle fileLength:info.fileSize forConnection:self];
                        }
                    }
                }
            }
        }
        
    }
    else if([path hasPrefix:@"/downloadcomplete/"]){
        NSRange range = [path rangeOfString:@"downloadcomplete" options:NSCaseInsensitiveSearch];
        NSString *filePath = [path substringFromIndex:range.location + range.length];
        PublicData *publicData = [PublicData sharedInstance];
        AssetsManager *assetsManager = [AssetsManager sharedInstance];
        if(publicData && assetsManager){
            if(!publicData.startDownLoad){
                return [[HTTPErrorResponse alloc] initWithErrorCode:200];
            }
            
            AssetInfo *info = [assetsManager assetWithPath:filePath];
            if(info){
                [assetsManager addCurrentExportedAsset:info];
                if(![publicData.currentExportingFile isEqualToString:filePath]){
                    //说明这个文件之前已经导出过，pc直接发的downloadcomplete命令，所以要加上该文件的大小，如果文件正在导出，在
                    //导出过程中已经将传输的大小累加到了havefreedSpaceSize。
                    publicData.havefreedSpaceSize += info.fileSize;
                }
                [[NSNotificationCenter defaultCenter] postNotificationName:NTF_ID_FILE_DOWNLAOD_SUCCESSED object:nil userInfo:@{@"file":filePath}];
            }
            return [[HTTPErrorResponse alloc] initWithErrorCode:200];
        }
    }
    
    return nil;
}

@end
