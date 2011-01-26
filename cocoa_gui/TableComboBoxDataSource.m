//
//  CCMLTableComboBoxDataSource.m
//  CCMLEditor
//
//  Created by Jeffrey Varner on 1/9/11.
//  Copyright 2011 Chemical and Biomolecular Engineering. All rights reserved.
//

#import "TableComboBoxDataSource.h"

@interface TableComboBoxDataSource (hiiden)

- (void)setup;
- (void)updateDataArray;
- (void)tableSelectionChanged:(NSNotification *)notification;
- (void)treeSelectionChanged:(NSNotification *)notification;

@end



@implementation TableComboBoxDataSource

// Synthesize -
@synthesize dataArray;
@synthesize xmlDocument;
@synthesize selectedXMLNode;

#pragma mark -----------------------------------
#pragma mark - Startup, and dealloc -
#pragma mark -----------------------------------
- (id) init
{
	self = [super init];
    if (self) 
	{
		[self setup];
	}
	
	// return me -
	return self;
}

// dealloc -
- (void)dealloc
{
	// Remove me from notification center -
	[[NSNotificationCenter defaultCenter] removeObserver:self];
	
	// Release my instance variables -
	[dataArray release];
	[xmlDocument release];
	[selectedXMLNode release];
	
	// Deallocate super -
	[super dealloc];
}

-(void)setup
{
	// Ok, so let's initialize the dataArray -
	self.dataArray = [[NSMutableArray alloc] initWithCapacity:10];
	
	// Ok, so we need to register for tree node selection did change events -
	[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(treeSelectionChanged:) name:NSOutlineViewSelectionDidChangeNotification object:nil];
	
	// Ok, so we also need to register for Table selection updates -
	[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(tableSelectionChanged:) name:NSTableViewSelectionDidChangeNotification object:nil];
}

-(void)awakeFromNib
{
	[self setup];
}

#pragma mark -----------------------------------
#pragma mark - Data source array methods -
#pragma mark -----------------------------------

- (id)comboBoxCell:(NSComboBoxCell *)aComboBoxCell objectValueForItemAtIndex:(NSInteger)index
{
	return [[self dataArray] objectAtIndex:index];
}

- (NSInteger)numberOfItemsInComboBoxCell:(NSComboBoxCell *)aComboBoxCell
{
	return [[self dataArray] count];
}

#pragma mark -------------------------------------------
#pragma mark - Tree/Table xml methods (populate list) -
#pragma mark -------------------------------------------
- (void) tableSelectionChanged:(NSNotification *)notification
{
	
	// Ok, when I get here the table selection changed -
	// 
	NSTableView *view = (NSTableView *)[notification object];
	
	// What row is selected?
	int rowIndex = [view selectedRow];
		
	// Make sure we have a row selected ...
	if (rowIndex!=-1)
	{
		// Ok, I have a legit selection, clear out the dataArray - the rest of the code 
		// is about repopulating the array - 
	
		// Try and get the wrapper -
		id notSureWhatThisIs =	[[view dataSource] tableView:view 
								  objectValueForTableColumn:[view tableColumnWithIdentifier:@"attribute"]
														row:rowIndex];
		
		if ([notSureWhatThisIs isEqual:@"package"])
		{
			[[self dataArray] removeAllObjects];
			
			// XPath for basal genes -
			// Ok, so If I get here, then I have a compartment key - we need to get the root document -
			NSXMLDocument *document = [[self selectedXMLNode] rootDocument];
			
			// Run XPath on this bitch to figure out what the available keys are ...
			NSMutableString *strXPath = [[NSMutableString alloc] initWithCapacity:140];
			[strXPath appendString:@".//package/@symbol"];
			
			// Execute the xpath call -
			NSError *err=nil; 
			NSArray *listOfBasalGenes = [document nodesForXPath:strXPath error:&err];
			for (NSXMLNode *node in listOfBasalGenes)
			{
				// Ok, **finally** so now we can add this to the dataArray -
				[[self dataArray] addObject:[node stringValue]];
			}
			
			// Ok, so now I need to have the view reLoad the data -
			[view reloadData];
			[strXPath release];
		}
		else if ([notSureWhatThisIs isEqual:@"path_symbol"])
		{
			[[self dataArray] removeAllObjects];
			
			// Ok, so If I get here, then I have a compartment key - we need to get the root document -
			NSXMLDocument *document = [[self selectedXMLNode] rootDocument];
			
			// Run XPath on this bitch to figure out what the available keys are ...
			NSMutableString *strXPath = [[NSMutableString alloc] initWithCapacity:140];
			[strXPath appendString:@".//ListOfPaths/path/@symbol"];
			
			// Execute the xpath call -
			NSError *err=nil; 
			NSArray *listOfCompartments = [document nodesForXPath:strXPath error:&err];
			for (NSXMLNode *node in listOfCompartments)
			{
				// Ok, **finally** so now we can add this to the dataArray -
				[[self dataArray] addObject:[node stringValue]];
			}
			
			// Ok, so now I need to have the view reLoad the data -
			[view reloadData];
			[strXPath release];
			
		}
		
		else if ([notSureWhatThisIs isEqual:@"input_classname"])
		{
			[[self dataArray] removeAllObjects];
			
			// Ok, so If I get here, then I have a compartment key - we need to get the root document -
			NSXMLDocument *document = [[self selectedXMLNode] rootDocument];
			
			// Run XPath on this bitch to figure out what the available keys are ...
			NSMutableString *strXPath = [[NSMutableString alloc] initWithCapacity:140];
			[strXPath appendString:@".//InputHandlerClasses/class/@classname"];
			
			// Execute the xpath call -
			NSError *err=nil; 
			NSArray *listOfCompartments = [document nodesForXPath:strXPath error:&err];
			for (NSXMLNode *node in listOfCompartments)
			{
				// Ok, **finally** so now we can add this to the dataArray -
				[[self dataArray] addObject:[node stringValue]];
			}
			
			// Ok, so now I need to have the view reLoad the data -
			[view reloadData];
			[strXPath release];
		}
		
		else if ([notSureWhatThisIs isEqual:@"output_classname"])
		{
			[[self dataArray] removeAllObjects];
			
			// Ok, so If I get here, then I have a compartment key - we need to get the root document -
			NSXMLDocument *document = [[self selectedXMLNode] rootDocument];
			
			// Run XPath on this bitch to figure out what the available keys are ...
			NSMutableString *strXPath = [[NSMutableString alloc] initWithCapacity:140];
			[strXPath appendString:@".//OutputHandlerClasses/class/@classname"];
			
			// Execute the xpath call -
			NSError *err=nil; 
			NSArray *listOfCompartments = [document nodesForXPath:strXPath error:&err];
			for (NSXMLNode *node in listOfCompartments)
			{
				// Ok, **finally** so now we can add this to the dataArray -
				[[self dataArray] addObject:[node stringValue]];
			}
			
			// Ok, so now I need to have the view reLoad the data -
			[view reloadData];
			[strXPath release];
		}
	}
	
	// Ok, so now I need to have the view reLoad the data -
	[view reloadData];
}

- (void) treeSelectionChanged:(NSNotification *)notification
{
	
	// Get the selecte in the tree -
	NSOutlineView *view = (NSOutlineView *)[notification object];

	// Get the selected row -
	int rowIndex = [view selectedRow];
	
	if (rowIndex!=-1)
	{
		// I need to check to see we respond -
		if ([view itemAtRow:rowIndex]!=nil)
		{
			// Get the treeNode -
			NSTreeNode *treeNode = (NSTreeNode *)[view itemAtRow:rowIndex];
			
			// Check to see if this guy responds to representedObject?
			if ([treeNode respondsToSelector:@selector(representedObject)])
			{
				// Ok, so if I make it this far, I have a legit selection -
					
				// Clear out the array -
				[[self dataArray] removeAllObjects];
				
				// Get the selected item in the tree -
				self.selectedXMLNode = (NSXMLElement *)[treeNode representedObject];
				
			}
		}
	} 	
}

@end
