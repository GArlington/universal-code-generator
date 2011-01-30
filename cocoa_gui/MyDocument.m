//
//  MyDocument.m
//  Translator
//
//  Created by Jeffrey Varner on 1/20/11.
//  Copyright 2011 Chemical and Biomolecular Engineering. All rights reserved.
//

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
	[translatorWindowController release];

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
	
	if (translatorWindowController==nil)
	{
		// Ok, we need to alloc init our custom window controller -
		translatorWindowController = [[TranslatorWindowController alloc] init];
		
		// Ok, we need to add this to the list of window controllers 
		[self addWindowController:translatorWindowController];
	}
	else 
	{
		// Not sure why I have this?
	}
	
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

	// Ok, from my window controller I need to get the xmlTree -
	NSXMLDocument *xmlDocument = [[translatorWindowController xmlTreeModel] xmlDocument];
	if (xmlDocument!=nil)
	{
		return [xmlDocument XMLData];
	}
	else {
		return nil;
	}
}

- (BOOL)readFromData:(NSData *)data ofType:(NSString *)typeName error:(NSError **)outError
{
    // Insert code here to read your document from the given data of the specified type.  If the given outError != NULL, ensure that you set *outError when returning NO.

    // You can also choose to override -readFromFileWrapper:ofType:error: or -readFromURL:ofType:error: instead. 
    
    // For applications targeted for Panther or earlier systems, you should use the deprecated API -loadDataRepresentation:ofType. In this case you can also choose to override -readFromFile:ofType: or -loadFileWrapperRepresentation:ofType: instead.
    
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
	
	
	
	if ( outError != NULL ) {
		*outError = [NSError errorWithDomain:NSOSStatusErrorDomain code:unimpErr userInfo:NULL];
	}
    return YES;
}

@end
