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
-(void)treeSelectionChanged:(NSNotification *)notification;
-(void)encodeObjectTree:(DDTreeNodeProxy *)treeWrapper xmlNode:(NSXMLElement *)node;

@end

@implementation OutlineViewDelegate

// Synthesize statements -
@synthesize iconModel;
@synthesize treeView;
@synthesize selectedXMLNode;

#pragma mark --------------------------------
#pragma mark init and dealloc
#pragma mark --------------------------------
-(void)awakeFromNib
{
	// Call the setup -
	[self setup];
}

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
    [selectedXMLNode release];
	
    // Release my tree view -
    self.treeView = nil;
	
	// dellocate my parent -
	[super dealloc];
}

-(void)setup
{
	// Initialize the system icons -
	self.iconModel = [TreeIconModel sharedInstance];
    
    // Register for notifications -
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(treeSelectionChanged:) name:NSOutlineViewSelectionDidChangeNotification object:nil];
    
    
    // Register the tree view for drag and drop activity?
	[[self treeView] registerForDraggedTypes:[NSArray arrayWithObjects:UNIVERSAL_TREE_NODE_TYPE,NSPasteboardTypeString, NSPasteboardTypePNG,nil]];
	[[self treeView] setDraggingSourceOperationMask:NSDragOperationEvery forLocal:YES];
	[[self treeView] setVerticalMotionCanBeginDrag:YES];
	[[self treeView] setAutoresizesOutlineColumn:NO];
}


#pragma mark --------------------------------
#pragma mark Notifications methods -
#pragma mark --------------------------------
-(void)treeSelectionChanged:(NSNotification *)notification
{
	// Ok, so when I get here the tree selection has changed -
	
    // Get local tree on this window -
    NSOutlineView *view = [self treeView];
    [view abortEditing];
    
	// Get the selected row -
	int selectedRow = [view selectedRow];
	
	if (selectedRow!=-1)
	{
		
		// Get the treeNode -
		NSTreeNode *node = (NSTreeNode *)[view itemAtRow:selectedRow];
		
		// Check to see if we call representedObject?
		if ([node respondsToSelector:@selector(representedObject)])
		{
			// Set my pointer to currently selected node -
            self.selectedXMLNode = (NSXMLElement *)[node representedObject];
            
        }
	}
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
	NSSize size = NSMakeSize (21, 21);
	
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

- (void)outlineView:(NSOutlineView *)outlineView willDisplayCell:(id)cell forTableColumn:(NSTableColumn *)tableColumn item:(id)item
{
    // Cast 2 the correct type -
    NSBrowserCell *browserCell = (NSBrowserCell *)cell;
    
    // Ok
    if ([browserCell isHighlighted])
    {
        [browserCell setBackgroundStyle:NSBackgroundStyleLight];
    }
    else
    {
        [browserCell setBackgroundStyle:NSBackgroundStyleLight];
    }
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
	
	// Provide data for our custom type, and simple NSStrings.
    [pboard declareTypes:[NSArray arrayWithObjects:UNIVERSAL_TREE_NODE_TYPE, nil] owner:self];
    
    // Build an array to hold list of node proxy's
    NSMutableArray *nodeArray = [[[NSMutableArray alloc] initWithCapacity:10] autorelease];
	
    // Get the selected node paths -
    if ([self selectedXMLNode]!=nil)
    {
        NSIndexSet *selectedIndexSet = [[self treeView] selectedRowIndexes];
        NSUInteger index=[selectedIndexSet firstIndex];
        while(index != NSNotFound) 
        {
            
            // Get the tree node -
            NSTreeNode *treeNode = (NSTreeNode *)[[self treeView] itemAtRow:index];
            
            // Get the xml node -
            NSXMLElement *xmlNode = [treeNode representedObject];
            
            // Ok, so if this is a leaf, then encode "normally"
            if ([xmlNode isLeaf])
            {
            
                // Archive the data from the selectedXMLNode -
                DDTreeNodeProxy *proxy = [[[DDTreeNodeProxy alloc] init] autorelease];
            
                // Ok, add the node to the proxy -
                [proxy setXmlNode:xmlNode];
            
                // Add the proxy's to the list -
                [nodeArray addObject:proxy];
            }
            else
            {
                // Create a root level wrapper --
                DDTreeNodeProxy *proxy = [[[DDTreeNodeProxy alloc] init] autorelease];
                
                // Ok, so this node has children ... how should I process this?
                [self encodeObjectTree:proxy xmlNode:xmlNode];
                
                // Ok, add to the array =
                [nodeArray addObject:proxy];
            }
                    
            // Get the next index -
            index=[selectedIndexSet indexGreaterThanIndex: index];
        }
    }
    
    // Add the node array to the paste board -
    [pboard setData:[NSKeyedArchiver archivedDataWithRootObject:nodeArray] forType:UNIVERSAL_TREE_NODE_TYPE]; 
    
   	
	// return yes -
	return YES;
}

-(void)encodeObjectTree:(DDTreeNodeProxy *)treeWrapper xmlNode:(NSXMLElement *)node
{
    // Ok, so if the xml node is a leaf -
    if ([node isLeaf])
    {
        // Ok, so I have an xmlNode that is a leaf (no children). Add me to the treeWrapper -
        treeWrapper.xmlNode = node;
    }
    else
    {
        // Ok, If I get here, then I have children. First, I need to add myself -
        treeWrapper.xmlNode = node;
        
        // Next, I need to go through the list of my kids and wrap them...
        NSArray *xmlKids = [node children];
        for (NSXMLElement *tmpXMLNode in xmlKids)
        {
            // Ok --
            
            // Create a new warpper -
            DDTreeNodeProxy *newWrapperNode = [[[DDTreeNodeProxy alloc] init] autorelease];
            
            // Wrap me -
            newWrapperNode.xmlNode = tmpXMLNode;
            
            // Add me (wrapper) to my parent -
            [treeWrapper addChild:newWrapperNode];
            
            // Ok, so now I need to call me ...
            [self encodeObjectTree:newWrapperNode xmlNode:tmpXMLNode];
        }
    }
}

-(BOOL)shouldCollapseAutoExpandedItemsForDeposited:(BOOL)deposited
{
	return YES;
}


-(BOOL)outlineView:(NSOutlineView *)ov acceptDrop:(id <NSDraggingInfo>)info item:(id)item childIndex:(NSInteger)childIndex
{
	// Method attributes -
	BOOL flag = NO;
	

	// Check to see if we have a legal childIndex (no negatives)
	if (childIndex!=-1)
	{
		// Get the posterboard -
		NSPasteboard *pb = [info draggingPasteboard];
        		
		// Get the types -
		if (pb!=nil)
		{
			// Get the array of types -
			NSArray *types = [pb types];
			
            // Check to see if we have the correct type -
			if ([types containsObject:UNIVERSAL_TREE_NODE_TYPE])
			{
				// Ok, so when I get here -- I have the *string* of the type that I need to create ... now what?
                // Get the data in the drop -
                NSData *dropData = [pb dataForType:UNIVERSAL_TREE_NODE_TYPE];
                
                // Get the mutable array from the archiver  -
                NSMutableArray *nodeArray = [NSKeyedUnarchiver unarchiveObjectWithData:dropData];
                
                // Go through the list -
                for (DDTreeNodeProxy *treeNodeProxy in nodeArray)
                {
                    // Ok, get the xml node from the proxy -
                    NSXMLElement *dropNode = [treeNodeProxy xmlNode];
                    
                    // Add the dropNode to the parent at index?
                    // Get my parent node and index -
                    NSXMLElement *parent = (NSXMLElement *)[item representedObject];
				
                    // Add the copy to the end of the list -
                    [parent insertChild:dropNode atIndex:childIndex];
				
                    // Ok, so I don't have access to the model - what can I do? I sent a notfication to refresh my pointer to the tree .. I should 
                    // figure out how to do this with the controller...all of this should be doable w/the OutlineController ...
                    NSString *MyNotificationName = @"RefreshTreeModel";
                    NSNotification *myNotification = [NSNotification notificationWithName:MyNotificationName object:nil]; 
				
                    // Send an update -
                    [[NSNotificationQueue defaultQueue] enqueueNotification:myNotification postingStyle:NSPostNow coalesceMask:NSNotificationCoalescingOnName forModes:nil];
                }
				
				// What did we get?
				//NSLog(@"Accept drop ...%@ at index = %d of parent %@",value,childIndex,[[self selectedXMLNode] displayName]);
			}
            
            // We have accepted the drop -
            flag = YES;
		}
	}
    else
    {
        // We will *not* accept the drop -
        flag = NO;
    }
	
	// return the boolean flag -
	return flag;
}

- (void)outlineView:(NSOutlineView *)outlineView didDragTableColumn:(NSTableColumn *)tableColumn
{
    NSLog(@"Starting to drag ...");
}



@end
