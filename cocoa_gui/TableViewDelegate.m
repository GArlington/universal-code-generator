// Copyright (c) 2011 Varner Lab, Cornell University

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
//  TableViewDelegate.m
//  Translator
//
//  Created by Jeffrey Varner on 1/21/11.

#import "TableViewDelegate.h"

// Private utility methods -
@interface TableViewDelegate (hidden)

-(void)setup;
-(void)treeSelectionChanged:(NSNotification *)notification;
-(void)openPathLocationPanelDidEnd:(NSOpenPanel *)openPanel returnCode:(int)returnCode contextInfo:(void *)contextInfo;
-(void)openFilenamePanelDidEnd:(NSOpenPanel *)openPanel returnCode:(int)returnCode contextInfo:(void *)contextInfo;
-(void)removeAttributeAlertEnded:(NSAlert *)alert returnCode:(int)code contextInfo:(void *)contextInfo;
-(void)repopulatePathString:(NSAlert *)alert returnCode:(int)code contextInfo:(void *)contextInfo;
-(void)openDirectoryPanel;
-(void)openFilenamePanel;
-(void)repopulateFilename:(NSAlert *)alert returnCode:(int)code contextInfo:(void *)contextInfo;

@end


@implementation TableViewDelegate

#pragma mark --------------------------------
#pragma mark synthesize statements
#pragma mark --------------------------------
@synthesize tableView;
@synthesize comboDataSource;
@synthesize tableData;
@synthesize selectedXMLNode;
@synthesize window;

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
	self.window = nil;
	
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
    [comboBoxCell setRefusesFirstResponder:YES];
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
-(void)tableView:(NSTableView *)tableView didClickTableColumn:(NSTableColumn *)tableColumn
{
    NSLog(@"Monkey ...");
}

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
	
	// This sets the background color on the drop down -
	if (rowIndex%2 == 0)
	{			
		if ([[aTableColumn identifier] isEqual:@"value"])
		{
			// Set the background on this cell -
			NSComboBoxCell *cell = (NSComboBoxCell *)aCell;
			[cell setBackgroundColor:[NSColor whiteColor]];
			
			// Ok, so let's check to see if we are in the required row -
            
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

#pragma mark --------------------------------
#pragma mark IBAction methods -
#pragma mark --------------------------------

-(IBAction)tableClicked:(id)sender
{
    // ok, load the row?
    [[self tableView] performClickOnCellAtColumn:0 row:[[self tableView] selectedRow]];
    
}


// =========================================================//
// Methods to add/remove items from the attribute table
// =========================================================//
// Add an attribute to the list of attributes --
-(IBAction)addAttribute:(id)sender
{
	
	// Need to make sure a tree node is selected -
	if ([self selectedXMLNode]!=nil)
	{
	
		// Method attributes -
		XMLAttributeWrapper *wrapper = [[XMLAttributeWrapper alloc] init];
		
		// Set some default mumbo jumbo on the wrapper -
		wrapper.attribute = @"<insert attribute name>";
		wrapper.value = @"<insert attribute value>";
		
		// Add the wrapper to the array -
		[[self tableData] addObject:wrapper];
		
		// Ok, we need to get the IndexSet that has been selected -
		NSIndexSet *newSelectedRow = [NSIndexSet indexSetWithIndex:([[self tableData] count] - 1)];
		
		// Reload the table -
		[[self tableView] reloadData];
		[[self tableView] selectRowIndexes:newSelectedRow byExtendingSelection:NO];
		[[self tableView] editColumn:0 row:([[self tableData] count] - 1) withEvent:nil select:YES];
		
		// release the wrapper object - retain count is 2!
		[wrapper release];
	}
}

-(void)openFilenamePanel
{
    // Get the array of file types and open the panel -
    NSOpenPanel *openPanel = [NSOpenPanel openPanel];
    
    // Configure the panel -
    [openPanel setAllowsMultipleSelection:NO];
    [openPanel setCanChooseFiles:YES];
    [openPanel setCanCreateDirectories:YES];
    
    // Get my current dir -
    NSString *bundlePath = [[[NSBundle mainBundle] bundlePath] stringByDeletingLastPathComponent];
    
    // Run the panel as a sheet -
    [openPanel beginSheetForDirectory:bundlePath
                                 file:nil 
                       modalForWindow:[self window]
                        modalDelegate:self 
                       didEndSelector:@selector(openFilenamePanelDidEnd:returnCode:contextInfo:)
                          contextInfo:NULL];
}

-(void)openDirectoryPanel
{
	// Get the array of file types and open the panel -
	NSOpenPanel *openPanel = [NSOpenPanel openPanel];
	
	// Configure the panel -
	[openPanel setAllowsMultipleSelection:NO];
	[openPanel setCanChooseFiles:NO];
	[openPanel setCanCreateDirectories:YES];
	[openPanel setCanChooseDirectories:YES];
	
	// Get my current dir -
	NSString *bundlePath = [[[NSBundle mainBundle] bundlePath] stringByDeletingLastPathComponent];
	
	// Run the panel as a sheet -
	[openPanel beginSheetForDirectory:bundlePath
								 file:nil 
					   modalForWindow:[self window]
						modalDelegate:self 
					   didEndSelector:@selector(openPathLocationPanelDidEnd:returnCode:contextInfo:)
						  contextInfo:NULL];
}

// Remove an attribute from the list --
-(IBAction)removeAttribute:(id)sender
{
	// Method attributes -
	int intSelectedRow = 0;
	int intNumberOfRows = 0;
	
	// Get the row that is currently selected in the table -
	intSelectedRow = [[self tableView] selectedRow];
	
	// How many rows does the table have?
	intNumberOfRows = [[self tableData] count];
	
	if (intSelectedRow < 0 || intSelectedRow>intNumberOfRows)
	{
		return;
	}
	else {
		
		// Ok, If I get here, the user has selected a valid row and pushed the delete button ... popup an alert to warn them?
		
		// First thing - stop editing
		[[self tableView] abortEditing];
		
		// What wrapper has been selected?
		XMLAttributeWrapper *tmpWrapper = [[self tableData] objectAtIndex:intSelectedRow];
		NSString *tmpAttributeName = [tmpWrapper attribute];
		
		// Popup the alert panel as a sheet - 
		NSAlert *alertPanel = [NSAlert alertWithMessageText:@"Delete attribute?" 
											  defaultButton:@"Delete"
											alternateButton:@"Cancel" 
												otherButton:nil 
								  informativeTextWithFormat:@"Do you really want to delete the %@ attribute?",tmpAttributeName]; 
		
		// Pop-up that mofo - when the user selects a button, the didEndSelector gets called
		[alertPanel beginSheetModalForWindow:[self window] 
							   modalDelegate:self 
							  didEndSelector:@selector(removeAttributeAlertEnded:returnCode:contextInfo:) 
								 contextInfo:NULL];	
	}
}

-(void)removeAttributeAlertEnded:(NSAlert *)alert returnCode:(int)code contextInfo:(void *)contextInfo
{
	
	// Method attributes -
	int intSelectedRow = 0;
	
	// Check to see what button has been pushed - the default is delete 
	if (code==NSAlertDefaultReturn)
	{
		
		// Get the row that is currently selected in the table -
		intSelectedRow = [[self tableView] selectedRow];
		
		// Get the name of the attribute that I'm going to nuke -
		XMLAttributeWrapper *tmpWrapper = [[self tableData] objectAtIndex:intSelectedRow];
		NSString *tmpAttributeName = [tmpWrapper attribute];
		
		// remove the row from the table -
		[[self tableData] removeObjectAtIndex:intSelectedRow];
		[[self tableView] reloadData];
		
		// We also need to remove that attribute from the xmlTree ...
		[[self selectedXMLNode] removeAttributeForName:tmpAttributeName];
	}	
}

-(IBAction)updateTableValues:(NSButton *)sender
{
	// Ok, when I get here, the user has pushed the "action" button for this table item. I need to get the current node and 
	// try to populate attributes with some "auto" values -
	
	// Method attributes -
	
	
	// Get the current selected node -
	NSXMLElement *currentNode = [self selectedXMLNode];
	if (currentNode!=nil)
	{
		// Ok, so if I get here, I have a non-nil selected node
		// I need to figure out what has been selected -
		
		// Think about this ... but for now, lets populate path_location
		NSXMLNode *attrPathLocationNode = [currentNode attributeForName:@"path_location"];
		NSXMLNode *filenameNode = [currentNode attributeForName:@"filename"];
		
		// Process the attr for path_location -
		if (attrPathLocationNode != nil)
		{
			// If I get here I have a path location - check to see is this gut is non-zero
			NSString *tmpPathString = [attrPathLocationNode stringValue];
			if ([tmpPathString length]==0)
			{
				// Ok, so I have an empty value here ..
				// Open up the panel -
				
				int rowIndex = [[self tableView] selectedRow];
				if (rowIndex != -1)
				{
					[self openDirectoryPanel];
				}
			}
			else 
			{
				
				int rowIndex = [[self tableView] selectedRow];
				if (rowIndex != -1)
				{
					// Ok, so If we get here I *already* have path data - ask the user if they would like to reset?
					// Popup the alert panel as a sheet - 
					NSAlert *alertPanel = [NSAlert alertWithMessageText:@"The path has already been specified." 
														  defaultButton:@"Yes"
														alternateButton:@"No" 
															otherButton:nil 
											  informativeTextWithFormat:@"Do you want to overwrite %@ ?",tmpPathString]; 
					
					// Pop-up that mofo - when the user selects a button, the didEndSelector gets called
					[alertPanel beginSheetModalForWindow:[self window] 
										   modalDelegate:self 
										  didEndSelector:@selector(repopulatePathString:returnCode:contextInfo:) 
											 contextInfo:NULL];	
				}
				
			}
		}
		else if (filenameNode !=nil)
		{
			// If I get here I have a path location - check to see is this gut is non-zero
			NSString *tmpPathString = [filenameNode stringValue];
			if ([tmpPathString length]==0)
			{
				// Ok, so I have an empty value here ..
				// Open up the panel -
				
				int rowIndex = [[self tableView] selectedRow];
				if (rowIndex != -1)
				{
                    [self openFilenamePanel];
				}
			}
            else
            {
                int rowIndex = [[self tableView] selectedRow];
				if (rowIndex != -1)
				{
					// Ok, so If we get here I *already* have path data - ask the user if they would like to reset?
					// Popup the alert panel as a sheet - 
					NSAlert *alertPanel = [NSAlert alertWithMessageText:@"The filename has already been specified." 
														  defaultButton:@"Yes"
														alternateButton:@"No" 
															otherButton:nil 
											  informativeTextWithFormat:@"Do you want to overwrite %@ ?",tmpPathString]; 
					
					// Pop-up that mofo - when the user selects a button, the didEndSelector gets called
					[alertPanel beginSheetModalForWindow:[self window] 
										   modalDelegate:self 
										  didEndSelector:@selector(repopulateFilename:returnCode:contextInfo:) 
											 contextInfo:NULL];	
				}
            }
		}
	}
}

-(void)repopulateFilename:(NSAlert *)alert returnCode:(int)code contextInfo:(void *)contextInfo
{
	// Check to see what button has been pushed - the default is overwite (yes) 
	if (code==NSAlertDefaultReturn)
	{
		// Let's create a timer have it fire in a couple of seconds -
		[NSTimer scheduledTimerWithTimeInterval:1.0 
										 target:self 
									   selector:@selector(openFilenamePanel) 
									   userInfo:nil 
										repeats:NO];
	}
}


-(void)repopulatePathString:(NSAlert *)alert returnCode:(int)code contextInfo:(void *)contextInfo
{
	// Check to see what button has been pushed - the default is overwite (yes) 
	if (code==NSAlertDefaultReturn)
	{
		// Let's create a timer have it fire in a couple of seconds -
		[NSTimer scheduledTimerWithTimeInterval:1.0 
										 target:self 
									   selector:@selector(openDirectoryPanel) 
									   userInfo:nil 
										repeats:NO];
	}
}

-(void)openFilenamePanelDidEnd:(NSOpenPanel *)openPanel returnCode:(int)returnCode contextInfo:(void *)contextInfo
{
	// Ok, check which button was pushed, get the path -
	if (returnCode == NSOKButton)
	{
		NSXMLElement *currentNode = [self selectedXMLNode];
		NSString *bundlePathString = [[openPanel filename] lastPathComponent];
		
		// Get the bundle path -
		// NSString *bundlePathString = [[[NSBundle mainBundle] bundlePath] stringByDeletingLastPathComponent];
		
		// Create dictionary -
		NSDictionary *tmpAttrTable = [[[NSDictionary alloc] initWithObjectsAndKeys:
									   bundlePathString,@"filename",nil] autorelease];
		
		// Update the node w/this path -
		[currentNode setAttributesAsDictionary:tmpAttrTable];
		
		// Ok, so we also need to update the GUI wrapper -
		int rowIndex = [[self tableView] selectedRow];
		
		if (rowIndex != -1)
		{
			// Grab the object for this row -
			XMLAttributeWrapper *wrapper = [[self tableData] objectAtIndex:rowIndex];
			
			// We need to update the wrapper value -
			[wrapper setValue:bundlePathString forKey:@"value"];
			
			// Update the table -
			[[self tableData] replaceObjectAtIndex:rowIndex withObject:wrapper];
			
			// reload the table -
			[[self tableView] reloadData];
			
			// Fire an event -
			NSString *MyNotificationName = @"TreeNodeDataChanged";
			NSNotification *myNotification = [NSNotification notificationWithName:MyNotificationName object:nil]; 
			[[NSNotificationQueue defaultQueue] enqueueNotification:myNotification postingStyle:NSPostNow coalesceMask:NSNotificationCoalescingOnName forModes:nil];
		}
		
	}
	
}
		
-(void)openPathLocationPanelDidEnd:(NSOpenPanel *)openPanel returnCode:(int)returnCode contextInfo:(void *)contextInfo
{
	// Ok, check which button was pushed, get the path -
	if (returnCode == NSOKButton)
	{
		NSXMLElement *currentNode = [self selectedXMLNode];
		NSString *bundlePathString = [openPanel directory];
		
		// Get the bundle path -
		// NSString *bundlePathString = [[[NSBundle mainBundle] bundlePath] stringByDeletingLastPathComponent];
		
		NSLog(@"What is the bundle path %@",bundlePathString);
		
		// Create dictionary -
		NSDictionary *tmpAttrTable = [[[NSDictionary alloc] initWithObjectsAndKeys:
									   bundlePathString,@"path_location",nil] autorelease];
		
		// Update the node w/this path -
		[currentNode setAttributesAsDictionary:tmpAttrTable];
		
		// Ok, so we also need to update the GUI wrapper -
		int rowIndex = [[self tableView] selectedRow];
		
		if (rowIndex != -1)
		{
			// Grab the object for this row -
			XMLAttributeWrapper *wrapper = [[self tableData] objectAtIndex:rowIndex];
			
			// We need to update the wrapper value -
			[wrapper setValue:bundlePathString forKey:@"value"];
			
			// Update the table -
			[[self tableData] replaceObjectAtIndex:rowIndex withObject:wrapper];
			
			// reload the table -
			[[self tableView] reloadData];
			
			// Fire an event -
			NSString *MyNotificationName = @"TreeNodeDataChanged";
			NSNotification *myNotification = [NSNotification notificationWithName:MyNotificationName object:nil]; 
			[[NSNotificationQueue defaultQueue] enqueueNotification:myNotification postingStyle:NSPostNow coalesceMask:NSNotificationCoalescingOnName forModes:nil];
		}
		
	}
}

@end
