// Copyright (c) 2011 Varner Lab, Chemical and Biomolecular Eng, Cornell 
// University

// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:

// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.

// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

//
//  MyCustomSheetController.m
//  Translator
//
//  Created by Jeffrey Varner on 2/6/11.
//

#import "MyCustomSheetController.h"

@interface MyCustomSheetController (hidden)

- (void)setup;
- (void)didEndSheet:(NSWindow *)sheet returnCode:(NSInteger)returnCode contextInfo:(void *)contextInfo;

@end


@implementation MyCustomSheetController

#pragma mark -----------------------------------------------
#pragma mark Synthesize statements 
#pragma mark -----------------------------------------------

@synthesize applicationWindow;
@synthesize localWindow;
@synthesize textLabel;
@synthesize selectedXMLNode;

#pragma mark -----------------------------------------------
#pragma mark init, deallocated, setup and windowdidLoad
#pragma mark -----------------------------------------------

- (id)initWithWindow:(NSWindow *)window
{
    self = [super initWithWindow:window];
    if (self) {
        
        // Call setup -
        [self setup];
    }
    
    return self;
}

- (void)dealloc
{
    // Deallocate the IBOutlets -
    self.applicationWindow = nil;
    self.localWindow = nil;
    self.textLabel = nil;
    
    // Deallocate my instance variables -
    [selectedXMLNode release];
    
    // Deallocate my 
    [super dealloc];
}

- (void)windowDidLoad
{
    [super windowDidLoad];
    
    // Implement this method to handle any initialization after your window controller's window has been loaded from its nib file.
}

- (void)setup
{
    
}

#pragma mark -----------------------------------------------
#pragma mark IBAction methods ---
#pragma mark -----------------------------------------------

- (IBAction)showCustomSheet:(NSWindow *)applicationWindowRef
{
    if ([self window]!=nil)
    {
        //Check the myCustomSheet instance variable to make sure the custom sheet does not already exist.
        [NSBundle loadNibNamed: @"MyCustomSheet" owner: self];
    
    }
    
    // Sheet is up here.
    // Return processing to the event loop
}

- (IBAction)closeCustomSheet:(NSButton *)sender
{
    [[self localWindow] orderOut:nil];
    [NSApp endSheet:[self localWindow]];
    NSLog(@"Monkey");
}

- (void)didEndSheet:(NSWindow *)sheet returnCode:(NSInteger)returnCode contextInfo:(void *)contextInfo
{
    NSLog(@"didEndSelect");
}

@end
