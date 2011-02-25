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

@implementation MyDocument

#pragma mark --------------------------------------
#pragma mark init and dealloc methods
#pragma mark --------------------------------------
- (id)init
{
    self = [super init];
    if (self) {
    
        // Add your subclass-specific initialization here.
        // If an error occurs here, send a [self release] message and return nil.
    
    }
    return self;
}

-(void)dealloc
{
	// release my instance variables -
    
    // Get the list of window controller and dealloc them?
    

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

// Override the makeWindowControllers method -
- (void)makeWindowControllers
{
	
    // Ok, we need to alloc init our custom window controller -
    TranslatorWindowController *translatorWindowController = [[TranslatorWindowController alloc] init];
		
    // Ok, we need to add this to the list of window controllers 
    [self addWindowController:translatorWindowController];
	
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

	/*
    // Ok, from my window controller I need to get the xmlTree -
	NSXMLDocument *xmlDocument = [[translatorWindowController xmlTreeModel] xmlDocument];
	if (xmlDocument!=nil)
	{
		return [xmlDocument XMLData];
	}
	else {
		return nil;
	}*/
    
    return nil;
}

- (BOOL)readFromData:(NSData *)data ofType:(NSString *)typeName error:(NSError **)outError
{
    // Insert code here to read your document from the given data of the specified type.  If the given outError != NULL, ensure that you set *outError when returning NO.

    // You can also choose to override -readFromFileWrapper:ofType:error: or -readFromURL:ofType:error: instead. 
    
    // For applications targeted for Panther or earlier systems, you should use the deprecated API -loadDataRepresentation:ofType. In this case you can also choose to override -readFromFile:ofType: or -loadFileWrapperRepresentation:ofType: instead.
    /*
    if (data!=nil)
	{
		// Get window controller -
		//CCMLProjectWindowController *controller = (CCMLProjectWindowController *)[[self windowControllers] lastObject];
		if (translatorWindowController!=nil)
		{
			[translatorWindowController createXMLDocumentFromData:data];
		}
		else {
			// Ok, we need to alloc init our custom window controller -
			translatorWindowController  = [[TranslatorWindowController alloc] init];
			
			// Ok, we need to add this to the list of window controllers 
			[self addWindowController:translatorWindowController];
			
			// Create the document -
			[translatorWindowController createXMLDocumentFromData:data];
			
		}
		
	}
     */
	
	
	
	if ( outError != NULL ) {
		*outError = [NSError errorWithDomain:NSOSStatusErrorDomain code:unimpErr userInfo:NULL];
	}
    return YES;
}

@end
