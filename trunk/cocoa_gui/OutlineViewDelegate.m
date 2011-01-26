//
//  OutlineViewDelegate.m
//  Translator
//
//  Created by Jeffrey Varner on 1/20/11.
//  Copyright 2011 Chemical and Biomolecular Engineering. All rights reserved.
//

#import "OutlineViewDelegate.h"
#import "NSXMLNode_Category.h"


// Private utility methods -
@interface OutlineViewDelegate (hidden)

-(void)setup;

@end

@implementation OutlineViewDelegate

@synthesize iconModel;

#pragma mark --------------------------------
#pragma mark init and dealloc
#pragma mark --------------------------------
-(id)init
{
	self = [super init];
	if (self!=nil)
	{
		// set me up -
		[self setup];
	}
	return self;
}

-(void)dealloc
{
	// release my instance variables -
	[iconModel release];
	
	
	// dellocate my parent -
	[super dealloc];
}

-(void)setup
{
	// Initialize the system icons -
	self.iconModel = [TreeIconModel sharedInstance];
}


#pragma mark --------------------------------
#pragma mark Methods to customize data cell
#pragma mark --------------------------------
// Called to customize tree cells -
-(NSCell *)outlineView:(NSOutlineView *)outlineView dataCellForTableColumn:(NSTableColumn *)tableColumn item:(id)item
{
	// Method attributes -
	NSBrowserCell *browserCell;
	
	// We need to figure out what type of object we are being passed in -
	// NSLog(@"What is the type of object being passed in - %@",[[item representedObject] className]);
	NSXMLNode *tmpNode = (NSXMLNode *)[item representedObject];
	
	// Create the browser image and set the image -
	browserCell = [[NSBrowserCell alloc] init];
	[browserCell setLeaf:YES];
	
	// Set the size -
	NSSize size = NSMakeSize (22, 22);
	
	// Ok, we need to check to see if a tag has a special icon -
	NSString *specialTag = [[self iconModel] getIconKeyForTagName:[tmpNode name]];
	if (specialTag!=nil)
	{
		
		// Ok, If I get here then I have a special tag -
		NSImage *tmpImage = [[self iconModel] getIconForKey:specialTag];
		[tmpImage setSize:size];
		[browserCell setImage:tmpImage];		
	}
	else 
	{
		// Check to see if this is a leaf -
		if ([tmpNode isLeaf])
		{
			NSImage *tmpImage = [[self iconModel] getIconForKey:@"LEAF"];
			[tmpImage setSize:size];
			[browserCell setImage:tmpImage];
		}
		else if ([tmpNode level]== 1)
		{
			NSImage *tmpImage = [[self iconModel] getIconForKey:@"ROOT"];
			[tmpImage setSize:size];
			[browserCell setImage:tmpImage];
			
			//[browserCell setImage:[[self iconTable] valueForKey:@"FOLDER"]];
		}
		else {
			
			NSImage *tmpImage = [[self iconModel] getIconForKey:@"FOLDER"];
			[tmpImage setSize:size];
			[browserCell setImage:tmpImage];
			
			//[browserCell setImage:[[self iconTable] valueForKey:@"FOLDER"]];
		}
	}
	
	
	[browserCell autorelease];
	return browserCell;	
}


@end
