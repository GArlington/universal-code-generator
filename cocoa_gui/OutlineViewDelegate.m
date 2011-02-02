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
