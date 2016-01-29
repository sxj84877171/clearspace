#import "HTTPAsyncVideoResponse.h"
#import "HTTPConnection.h"
#import "HTTPLogging.h"

#import <unistd.h>
#import <fcntl.h>

#if ! __has_feature(objc_arc)
#warning This file must be compiled with ARC. Use -fobjc-arc flag (or convert project to ARC).
#endif

// Log levels : off, error, warn, info, verbose
// Other flags: trace
static const int httpLogLevel = HTTP_LOG_LEVEL_WARN; // | HTTP_LOG_FLAG_TRACE;

#define NULL_FD  -1

/**
 * Architecure overview:
 * 
 * HTTPConnection will invoke our readDataOfLength: method to fetch data.
 * We will return nil, and then proceed to read the data via our readSource on our readQueue.
 * Once the requested amount of data has been read, we then pause our readSource,
 * and inform the connection of the available data.
 * 
 * While our read is in progress, we don't have to worry about the connection calling any other methods,
 * except the connectionDidClose method, which would be invoked if the remote end closed the socket connection.
 * To safely handle this, we do a synchronous dispatch on the readQueue,
 * and nilify the connection as well as cancel our readSource.
 * 
 * In order to minimize resource consumption during a HEAD request,
 * we don't open the file until we have to (until the connection starts requesting data).
**/

@implementation HTTPAsyncVideoResponse{
}

- (id)initWithFileHandle:(NSFileHandle *)fileHandle fileLength:(UInt64)length forConnection:(HTTPConnection *)parent
{
    if ((self = [super init]))
    {
        HTTPLogTrace();
        
        connection = parent; // Parents retain children, children do NOT retain parents
        
        _fileHandle = fileHandle;
         fileLength = length;
         fileOffset = 0;
        
        // We don't bother opening the file here.
        // If this is a HEAD request we only need to know the fileLength.
    }
    return self;
}
- (UInt64)contentLength
{
    return fileLength;
}

- (UInt64)offset
{
    HTTPLogTrace();
    
    return offset;
}

- (void)setOffset:(UInt64)offsetParam
{
    HTTPLogTrace2(@"%@[%p]: setOffset:%lu", THIS_FILE, self, (unsigned long)offset);
    
    offset = (NSUInteger)offsetParam;
}

- (NSData *)readDataOfLength:(NSUInteger)lengthParameter
{
    HTTPLogTrace2(@"%@[%p]: readDataOfLength:%lu", THIS_FILE, self, (unsigned long)lengthParameter);
    
    NSUInteger remaining = fileLength - offset;
    NSUInteger length = lengthParameter < remaining ? lengthParameter : remaining;
    
    if(_fileHandle){
        [_fileHandle seekToFileOffset:offset];
        offset += length;
        return [_fileHandle readDataOfLength:length];
    }
    
    
    return nil;
}

- (BOOL)isDone
{
    BOOL result = (offset == fileLength);
    
    HTTPLogTrace2(@"%@[%p]: isDone - %@", THIS_FILE, self, (result ? @"YES" : @"NO"));
    
    return result;
}

@end
