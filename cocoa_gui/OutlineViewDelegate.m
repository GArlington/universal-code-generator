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


#import "OutlineViewDelegate.h"
#import "NSXMLNode_Category.h"

// Define the node type for drag and drop -
#define UNIVERSAL_TREE_NODE_TYPE    @"MyUNIVERSALTreeNodeTypePboardType"

// Private utility methods -
@interface OutlineViewDelegate (hidden)

-(void)setup;

@end

@implementation OutlineViewDelegate

// Synthesize statements -
@synthesize iconModel;
@synthesize treeView;

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
	
    // Release my tree view -
    self.treeView = nil;
	
	// dellocate my parent -
	[super dealloc];
}

-(void)setup
{
	// Initialize the system icons -
	self.iconModel = [TreeIconModel sharedInstance];
    
    // Register the tree view for drag and drop activity?
	[[self treeView] registerForDraggedTypes:[NSArray arrayWithObjects:UNIVERSAL_TREE_NODE_TYPE,NSPasteboardTypeString, NSPasteboardTypePNG,nil]];
	[[self treeView] setDraggingSourceOperationMask:NSDragOperationEvery forLocal:YES];
	[[self treeView] setDraggingSourceOperationMask:NSDragOperationEvery forLocal:NO];
	[[self treeView] setVerticalMotionCanBeginDrag:YES];
	[[self treeView] setAutoresizesOutlineColumn:NO];
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

-(BOOL)outlineView:(NSOutlineView *)outlineView shouldEditTableColumn:(NSTableColumn *)tableColumn item:(id)item
{
    return NO;
}

#pragma mark ------------------------------------------
#pragma mark  - Drag and Drop methods -
#pragma mark ------------------------------------------
- (BOOL)outlineView:(NSOutlineView *)outlineView isGroupItem:(id)item 
{
	BOOL flag = NO;
	NSXMLElement *node = (NSXMLElement *)[item representedObject];
	
	if ([node isLeaf])
	{
		flag = NO;
	}
	else {
		flag = NO;
	}
	
	
	return flag;
}

-(BOOL)outlineView:(NSOutlineView *)ov shouldSelectItem:(id)item
{
	return YES;
}

-(void)outlineViewAction:(id)sender
{
}

-(NSDragOperation)outlineView:(NSOutlineView *)ov validateDrop:(id <NSDraggingInfo>)info proposedItem:(id)item proposedChildIndex:(NSInteger)childIndex
{
	NSDragOperation result = NSDragOperationCopy;
	return result;
}


-(BOOL)outlineView:(NSOutlineView *)outlineView writeItems:(NSArray *)items toPasteboard:(NSPasteboard *)pboard
{
	
	// Get the selected item -
    
    NSLog(@"Drag...started...");
    
	// Provide data for our custom type, and simple NSStrings.
    [pboard declareTypes:[NSArray arrayWithObjects:UNIVERSAL_TREE_NODE_TYPE,NSStringPboardType, nil] owner:self];
	
    // the actual data doesn't matter since SIMPLE_BPOARD_TYPE drags aren't recognized by anyone but us!.
    [pboard setData:[NSData data] forType:UNIVERSAL_TREE_NODE_TYPE]; 
    
    // Put string data on the pboard... notice you can drag into TextEdit!
    [pboard setString:@"UNIVERSAL_SPECIFICATION_TREE_NODE" forType:UNIVERSAL_TREE_NODE_TYPE];
	
	// return yes -
	return YES;
}

-(BOOL)shouldCollapseAutoExpandedItemsForDeposited:(BOOL)deposited
{
	return YES;
}


-(BOOL)outlineView:(NSOutlineView *)ov acceptDrop:(id <NSDraggingInfo>)info item:(id)item childIndex:(NSInteger)childIndex
{
	// Method attributes -
	BOOL flag = NO;
	
	
	NSLog(@"Going to accept the drop for %@",[item description]);
	
	// Check to see if we have a legal childIndex (no negatives)
	if (childIndex!=-1)
	{
		// Get the posterboard -
		NSPasteboard *pb = [NSPasteboard pasteboardWithName:NSDragPboard];
		
		// Get the types -
		if (pb!=nil)
		{
			// Get the array of types -
			NSArray *types = [pb types];
			
            // Check to see if we have the correct type -
			if ([types containsObject:UNIVERSAL_TREE_NODE_TYPE])
			{
				// Get the string -
				//NSString *value = [pb stringForType:UNIVERSAL_TREE_NODE_TYPE];
				
				// Ok, so when I get here -- I have the *string* of the type that I need to create ... now what?
				
				// Ok, first create a new xml element -
				//NSXMLElement *dropNode = [[[NSXMLElement alloc] initWithName:value] autorelease];
				
				// Ok, so I need to determine if I the guy has an attribute -
				// 
				
				// Setup the XPath query string -
				NSMutableString *strXPath = [[NSMutableString alloc] initWithCapacity:140];
                
				// Add the dropNode to the parent at index?
				//[[self s] insertChild:dropNode atIndex:childIndex];
				
				// Ok, so I don't have access to the model - what can I do? I sent a notfication to refresh my pointer to the tree .. I should 
				// figure out how to do this with the controller...all of this should be doable w/the OutlineController ...
				NSString *MyNotificationName = @"RefreshTreeModel";
				NSNotification *myNotification = [NSNotification notificationWithName:MyNotificationName object:nil]; 
				
				// Send an update -
				[[NSNotificationQueue defaultQueue] enqueueNotification:myNotification postingStyle:NSPostNow coalesceMask:NSNotificationCoalescingOnName forModes:nil];
				
				// What did we get?
				//NSLog(@"Accept drop ...%@ at index = %d of parent %@",value,childIndex,[[self selectedXMLNode] displayName]);
				flag = YES;
				
				// Release the xpath string -
				[strXPath release];
			}
		}
	}
	
	// return the boolean flag -
	return flag;
}

- (void)outlineView:(NSOutlineView *)outlineView didDragTableColumn:(NSTableColumn *)tableColumn
{
    NSLog(@"Starting to drag ...");
}



@end
