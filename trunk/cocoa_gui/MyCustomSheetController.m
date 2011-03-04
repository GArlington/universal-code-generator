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
    // Populate the value of the textLabel -
    [[self textLabel] setStringValue:[[self selectedXMLNode] name]];
    
    // Change the title on the window -
    [[self window] setTitle:@"Edit the name of the tree node ..."];
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
        
        // Call setup -
        [self setup];
        
        // Make sure it is front -
        [[self window] performSelector:@selector(makeKeyAndOrderFront:) withObject:nil afterDelay:0.0];
    }
    
    // Sheet is up here.
    // Return processing to the event loop
}

- (IBAction)closeCustomSheet:(NSButton *)sender
{
    [[self localWindow] orderOut:nil];
    [NSApp endSheet:[self localWindow]];
}

- (IBAction)updateNodeName:(NSButton *)sender
{
    // Ok, so when I get here, I need to grab the text from label and set the name on the node -
    NSString *strNewName = [[self textLabel] stringValue];
    
    // Ok, set the name on the selected XML node -
    if ([strNewName length]!=0)
    {
        // Set the text -
        [[self selectedXMLNode] setName:strNewName];
        
        // Set the dirty button -
        //[[self applicationWindow] setDocumentEdited:YES];
        
        // Have the tree reload data -
        NSString *MyNotificationName = @"RefreshTreeModel";
        NSNotification *myNotification = [NSNotification notificationWithName:MyNotificationName object:nil]; 
        
        // Send an update -
        [[NSNotificationQueue defaultQueue] enqueueNotification:myNotification postingStyle:NSPostNow coalesceMask:NSNotificationCoalescingOnName forModes:nil];
    }

}

- (IBAction)changeNodeName:(NSButton *)sender
{
    // Ok, so when I get here, I need to grab the text from label and set the name on the node -
    NSString *strNewName = [[self textLabel] stringValue];
    
    // Ok, set the name on the selected XML node -
    if ([strNewName length]!=0)
    {
        // Set the text -
        [[self selectedXMLNode] setName:strNewName];
        
        // Set the dirty button -
        //[[self applicationWindow] setDocumentEdited:YES];
        
        // Have the tree reload data -
        NSString *MyNotificationName = @"TreeNodeDataChanged";
        NSNotification *myNotification = [NSNotification notificationWithName:MyNotificationName object:nil]; 
        
        // Send an update -
        [[NSNotificationQueue defaultQueue] enqueueNotification:myNotification postingStyle:NSPostNow coalesceMask:NSNotificationCoalescingOnName forModes:nil];

        
        // Close me -
        [[self localWindow] orderOut:nil];
        [NSApp endSheet:[self localWindow]];
    }
}

- (void)sheetDidEnd:(NSWindow *)sheet returnCode:(NSInteger)returnCode contextInfo:(void *)contextInfo
{
    
}


@end
