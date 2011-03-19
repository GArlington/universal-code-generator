// Copyright (c) 2011 Varner Lab

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
//  MyDocument.m
//  Translator
//
//  Created by Jeffrey Varner on 1/20/11.

#import "MyDocument.h"

@interface MyDocument (hidden)

-(void)setup;
-(void)updateDocumentState:(id)sender;

@end;

@implementation MyDocument

@synthesize dataFromFile;
@synthesize localWindowController;
@synthesize editedTimer;
@synthesize editedFlag;

#pragma mark --------------------------------------
#pragma mark init and dealloc methods
#pragma mark --------------------------------------
- (void)setup
{
    
    /*
    // Create a timer instance -
    NSTimer *timer = [NSTimer scheduledTimerWithTimeInterval:2.0 
                                                      target:self 
                                                    selector:@selector(updateDocumentState:)
                                                    userInfo:nil 
                                                     repeats:YES];
     
    
    // Grab the instance -
    self.editedTimer = timer;
     */
}

- (id)init
{
    self = [super init];
    if (self) {
    
        // Add your subclass-specific initialization here.
        // If an error occurs here, send a [self release] message and return nil.
        [self setup];
    }
    return self;
}

-(void)dealloc
{
	// release my instance variables -
    
    // Get the list of window controller and dealloc them?
    self.dataFromFile = nil;
    self.localWindowController = nil;
    self.editedTimer = nil;
    
	// Deallocate my super -
	[super dealloc];
}


/*
- (NSString *)windowNibName
{
    // Override returning the nib file name of the document
    // If you need to use a subclass of NSWindowController or if your document supports multiple NSWindowControllers, you should remove this method and override -makeWindowControllers instead.
    return @"MyDocument";
}*/


#pragma mark -
#pragma mark Override methods - 
#pragma mark -
// Override the makeWindowControllers method -
- (void)makeWindowControllers
{
    // Ok, we need to alloc init our custom window controller -
    TranslatorWindowController *translatorWindowController = [[TranslatorWindowController alloc] init];
    
    // Check to see if we have data to hand to the controller -
    if ([self dataFromFile]!=nil)
    {
        [translatorWindowController createXMLDocumentFromData:[self dataFromFile]];
    }
    
    // Ok, we need to add this to the list of window controllers 
    [self addWindowController:translatorWindowController];
    
    // NSLog(@"The controller retain count is - %lu",[translatorWindowController retainCount]);
    
    // Release the controller -
    self.localWindowController = translatorWindowController;
    
    // NSLog(@"The controller retain count is - %lu",[[self localWindowController] retainCount]);
    
    // NSLog(@"We just made a windowcontroller. We know have %lu controllers",[[self windowControllers] count]);
    
	
}



- (void)windowControllerDidLoadNib:(NSWindowController *) aController
{
    [super windowControllerDidLoadNib:aController];
    // Add any code here that needs to be executed once the windowController has loaded the document's window.
}

- (NSData *)dataOfType:(NSString *)typeName error:(NSError **)outError
{
    // Insert code here to write your document to data of the specified type. If the given outError != NULL, ensure that you set *outError when returning nil.

    // You can also choose to override -fileWrapperOfType:error:, -writeToURL:ofType:error:, or -writeToURL:ofType:forSaveOperation:originalContentsURL:error: instead.

    // For applications targeted for Panther or earlier systems, you should use the deprecated API -dataRepresentationOfType:. In this case you can also choose to override -fileWrapperRepresentationOfType: or -writeToFile:ofType: instead.

    // Get the controller -
    TranslatorWindowController *controller = (TranslatorWindowController *)[[self windowControllers] lastObject];     
	
    if (controller !=nil)
    {
        // Ok, from my window controller I need to get the xmlTree -
        NSXMLDocument *xmlDocument = [[controller xmlTreeModel] xmlDocument];
        
        if (xmlDocument!=nil)
        {
            return [xmlDocument XMLDataWithOptions:NSXMLNodePrettyPrint];
        }
        else {
            return nil;
        }
    }
    
    return nil;
}

- (BOOL)readFromData:(NSData *)data ofType:(NSString *)typeName error:(NSError **)outError
{
    // Insert code here to read your document from the given data of the specified type.  If the given outError != NULL, ensure that you set *outError when returning NO.

    // You can also choose to override -readFromFileWrapper:ofType:error: or -readFromURL:ofType:error: instead. 
    
    // For applications targeted for Panther or earlier systems, you should use the deprecated API -loadDataRepresentation:ofType. In this case you can also choose to override -readFromFile:ofType: or -loadFileWrapperRepresentation:ofType: instead.
    
    // Get the controller -
    // NSLog(@"readFromNSdata ...");
    
    if (data!=nil)
    {
        
        self.dataFromFile = data;
        //NSLog(@"readFromNSdata ...branch two. We have %lu window controllers",[[self windowControllers] count]);
        
    }
	
	if ( outError != NULL ) {
		*outError = [NSError errorWithDomain:NSOSStatusErrorDomain code:unimpErr userInfo:NULL];
	}
    return YES;
}


-(void)saveDocument:(id)sender
{
    // Save the document -
    [super saveDocument:sender];
    
    // Update the button enabled state -
    [[[self localWindowController] codeGeneratorButton] setEnabled:YES];
}

-(void)saveDocumentAs:(id)sender
{
    // Save the document -
    [super saveDocumentAs:sender];
    
    // Update the button enabled state -
    [[[self localWindowController] codeGeneratorButton] setEnabled:YES];
}

#pragma mark ----------------------------------------------
#pragma mark - Static class methods --
#pragma mark ----------------------------------------------
+ (BOOL)canConcurrentlyReadDocumentsOfType:(NSString *)typeName
{
	return YES;
}

#pragma mark -
#pragma mark IBAction methods 
#pragma mark -
-(IBAction)saveSpecificationTree:(id)sender
{
    // Save the document -
    [self saveDocument:sender];
    
    // Update the 
}

#pragma mark -
#pragma mark Document timer methods - 
#pragma mark -
-(void)updateDocumentState:(id)sender
{
    
    NSLog(@"Updating state...");
    
}


@end
