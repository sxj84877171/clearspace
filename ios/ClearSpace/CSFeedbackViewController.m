//
//  CSFeedbackViewController.m
//  ClearSpace
//
//  Created by Kevin  on 11/25/15.
//  Copyright © 2015 SW2. All rights reserved.
//

#import "CSFeedbackViewController.h"
#import "statistics/CSStatisticsFactory.h"

@interface CSFeedbackViewController () <UITextViewDelegate>
@property IBOutlet UITextView * inputTextView;
@end

@implementation CSFeedbackViewController
{
    NSString * placeHolderString;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    placeHolderString = @"请输入你的宝贵意见";
    
    self.inputTextView.delegate = self;
    self.inputTextView.text = placeHolderString;
    self.inputTextView.textColor = [UIColor lightGrayColor];
    
    UIBarButtonItem *barButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemDone target:self.inputTextView action:@selector(resignFirstResponder)];
    UIToolbar *toolbar = [[UIToolbar alloc] initWithFrame:CGRectMake(0, 0, 320, 44)];
    toolbar.items = [NSArray arrayWithObject:barButton];
    
    self.inputTextView.inputAccessoryView = toolbar;
}

-(void)viewWillAppear:(BOOL)animated
{
    [[CSStatisticsFactory shareDefaultInstance] viewWillAppear:@"CSFeedbackViewController"];
}
-(void)viewWillDisappear:(BOOL)animated{
    [super viewWillDisappear:animated];
    [[CSStatisticsFactory shareDefaultInstance] viewWillDisappear:@"CSFeedbackViewController"];
}

- (void)textViewDidBeginEditing:(UITextView *)textView
{
    if ([textView.text isEqualToString:placeHolderString]) {
        textView.text = @"";
        textView.textColor = [UIColor blackColor]; //optional
    }
    [textView becomeFirstResponder];
}

- (void)textViewDidEndEditing:(UITextView *)textView
{
    if ([textView.text isEqualToString:@""]) {
        textView.text = placeHolderString;
        textView.textColor = [UIColor lightGrayColor]; //optional
    }
    [textView resignFirstResponder];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

-(IBAction)btnCommitFeedbackTouchUpInside:(id)sender
{
    if ([self commitFeedback]) {
        [self.navigationController popViewControllerAnimated:YES];
    }
    else
    {
        //error
        NSLog(@"submit feedback fail");
    }
}

-(BOOL)commitFeedback
{
    static NSString * feedbackUri = @"http://115.29.178.5/feedback/publish/";
    
    NSURL *url=[NSURL URLWithString:feedbackUri];
    NSMutableURLRequest *request = [[NSMutableURLRequest alloc]initWithURL:url cachePolicy:NSURLRequestUseProtocolCachePolicy timeoutInterval:10];
    [request setHTTPMethod:@"POST"];
    
    //set post data
    NSString * content = [self.inputTextView text];
    NSString *poststr = [NSString stringWithFormat:
                         @"content=%@&devicetype=ios&username=%@",content,[UIDevice currentDevice].name];
    
    NSData *postdata = [poststr dataUsingEncoding:NSUTF8StringEncoding];
    [request setHTTPBody:postdata];
    
    [NSURLConnection sendAsynchronousRequest:request queue:[[NSOperationQueue alloc] init] completionHandler:^(NSURLResponse * _Nullable response, NSData * _Nullable data, NSError * _Nullable connectionError) {
        if (connectionError) {
            NSLog(@"error in sending feedback :%@",connectionError);
        }
    }];

    return YES;
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
