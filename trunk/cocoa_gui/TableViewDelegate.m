//
//  TableViewDelegate.m
//  Translator
//
//  Created by Jeffrey Varner on 1/21/11.
//  Copyright 2011 Chemical and Biomolecular Engineering. All rights reserved.
//

#import "TableViewDelegate.h"

// Private utility methods -
@interface TableViewDelegate (hidden)

-(void)setup;
-(void)treeSelectionChanged:(NSNotification *)notification;

@end


@implementation TableViewDelegate

#pragma mark --------------------------------
#pragma mark synthesize statements
#pragma mark --------------------------------
@synthesize tableView;
@synthesize comboDataSource;
@synthesize tableData;
@synthesize selectedXMLNode;

#pragma mark --------------------------------
#pragma mark init and dealloc
#pragma mark --------------------------------
-(id)init
{
	self = [super init];
	if (self!=nil)
	{
		// set me up ..
		[self setup];
	}
	return self;
}


-(void)dealloc
{
	// release my connection to the notification center -
	[[NSNotificationCenter defaultCenter] removeObserver:self];
	
	// release my instance variables -
	[tableData release];
	[comboDataSource release];
	[selectedXMLNode release];
	
	// release my outlets -
	self.tableView = nil;
	
	// deallocate my super ...
	[super dealloc];
}

-(void)awakeFromNib
{
	[self setup];
}

-(void)setup
{
	// Ok, fire up the combo box data source -
	self.comboDataSource = [[TableComboBoxDataSource alloc] init];
	
	// Configure the table to use combobox data cells -
	NSComboBoxCell *comboBoxCell = [[NSComboBoxCell alloc] init];
	[comboBoxCell setUsesDataSource:YES];
	[comboBoxCell setDataSource:[self comboDataSource]];
	[comboBoxCell setButtonBordered:NO];
	[comboBoxCell setCompletes:YES];
	[comboBoxCell setDrawsBackground:YES];
	[comboBoxCell setBezeled:NO];
	[comboBoxCell setEnabled:YES];
	[comboBoxCell setEditable:YES];
	[comboBoxCell setItemHeight:22.0];
	[comboBoxCell setAction:@selector(valueComboBoxCellAction:)];
	[comboBoxCell setTarget:self];
	
	// We want the combo box on the value col -
	NSTableColumn *valTableCol = [[self tableView] tableColumnWithIdentifier:@"value"];
	
	[valTableCol setDataCell:comboBoxCell];
	[comboBoxCell release];
	
	// Setup the table data source -
	self.tableData = [[NSMutableArray alloc] initWithCapacity:4.0];
	
	// Ok, so we need to listen to some notifications -
	[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(treeSelectionChanged:) name:NSOutlineViewSelectionDidChangeNotification object:nil];
	
}

#pragma mark --------------------------------
#pragma mark Notification center methods -
#pragma mark --------------------------------
-(void)treeSelectionChanged:(NSNotification *)notification
{
	// Ok, when I get here -- the tree node selection has changed
	// Get the selected object and xml node -
	NSOutlineView *view = (NSOutlineView *)[notification object];
	
	// Get the selected row -
	int selectedRow = [view selectedRow];
	
	if (selectedRow!=-1)
	{
		// Get the treeNode -
		NSTreeNode *node = (NSTreeNode *)[view itemAtRow:selectedRow];
		
		// Check to see if we call representedObject?
		if ([node respondsToSelector:@selector(representedObject)])
		{
			// Grab the selected node -
			self.selectedXMLNode = (NSXMLElement *)[node representedObject];
			
			// Ok, so I need to put the attributes names and values in a property wrapper and put them in the array -
			NSArray *attributesList = [[self selectedXMLNode] attributes];
			
			// Retain the list for the moment -
			[attributesList retain];
			
			// Ok, so I need to go through this list, wrap the values and add them to my mutable array -
			// First I need to clean my array -
			[[self tableData] removeAllObjects];
			
			// Iterate through --
			for (NSXMLNode *node in attributesList)
			{
				// Wrap the data -
				XMLAttributeWrapper *wrapper = [[XMLAttributeWrapper alloc] init];
				wrapper.attribute = [node name];
				wrapper.value = [node stringValue];
				
				// Add to the array -
				[[self tableData] addObject:wrapper];
				
				// release the wrapper object - retain count is 2!
				// NSLog(@"What is the retain count on wrapper? %d",[wrapper retainCount]);
				[wrapper release];
			}
			
			// release the list -
			[attributesList release];
		}
	}
	
		
	// Ok, so we know the tree selection changed - we need to *force* the table to reload its data -
	[[self tableView] reloadData];
}

#pragma mark --------------------------------
#pragma mark TableDataSource methods
#pragma mark --------------------------------
-(NSInteger)numberOfRowsInTableView:(NSTableView *)aTableView
{
	// Method attributes -
	NSInteger intValue = 0;
	
	// Ok, grab the data table and get its size -
	intValue = [[self tableData] count];
	
	// return that value ...
	return intValue;
}

-(id)tableView:(NSTableView *)aTableView objectValueForTableColumn:(NSTableColumn *)aTableColumn row:(NSInteger)rowIndex;
{
	// Method attributes -
	
	// Have no idea what this does??
	NSString *identifier = [aTableColumn identifier];
	
	// Ok, next .. 
	XMLAttributeWrapper *wrapper = [[self tableData] objectAtIndex:rowIndex];
	
	// NSLog(@"In objectValueForTable method - name = %@ and value = %@. What is indentifier? WTF - %@",[wrapper attribute],[wrapper value],identifier);
	
	if (identifier!=nil)
	{
		// This I also don't get -
		return [wrapper valueForKey:identifier];
	}
	else
	{
		return nil;
	}
	
}

// Captures the user input when editing the table
- (void)tableView:(NSTableView *)aTableView setObjectValue:(id)anObject forTableColumn:(NSTableColumn *)aTableColumn row:(NSInteger)rowIndex
{
	// Method attributes -
	NSString *newColValue = (NSString *)anObject;
	
	// Get the table "key" - this is set in interface builder 
	NSString *identifier = [aTableColumn identifier];
	
	// Grab the object for this row -
	XMLAttributeWrapper *wrapper = [[self tableData] objectAtIndex:rowIndex];
	NSString *tmpOldAttributeName = [wrapper attribute];
	
	// We need to update the wrapper value -
	[wrapper setValue:newColValue forKey:identifier];
	
	// Update the table -
	[[self tableData] replaceObjectAtIndex:rowIndex withObject:wrapper];
	
	// reload the table -
	[[self tableView] reloadData];
	
	// Ok, so when I get here I need to update the xml node associated with this row -
	// First - remove the attribute object for this node -
	[[self selectedXMLNode] removeAttributeForName:tmpOldAttributeName];
	
	// Get the latest values for the attribute and value from the wrapper -
	NSXMLNode *tmpNode = [NSXMLNode attributeWithName:[wrapper attribute] stringValue:[wrapper value]];
	
	// Add to the current selected node -
	[[self selectedXMLNode] addAttribute:tmpNode];
	
	// Have the tree reload data -
	NSString *MyNotificationName = @"TreeNodeDataChanged";
	NSNotification *myNotification = [NSNotification notificationWithName:MyNotificationName object:nil]; 
	
	// Send an update -
	[[NSNotificationQueue defaultQueue] enqueueNotification:myNotification postingStyle:NSPostNow coalesceMask:NSNotificationCoalescingOnName forModes:nil];
}

- (void)tableView:(NSTableView *)aTableView willDisplayCell:(id)aCell forTableColumn:(NSTableColumn *)aTableColumn row:(NSInteger)rowIndex
{
	if (rowIndex%2 == 0)
	{
		if ([[aTableColumn identifier] isEqual:@"value"])
		{
			NSComboBoxCell *cell = (NSComboBoxCell *)aCell;
			[cell setBackgroundColor:[NSColor whiteColor]];
		}
	}
	else {
		if ([[aTableColumn identifier] isEqual:@"value"])
		{
			NSComboBoxCell *cell = (NSComboBoxCell *)aCell;
			[cell setBackgroundColor:[NSColor windowBackgroundColor]];
		}		
	}
	
}




@end
