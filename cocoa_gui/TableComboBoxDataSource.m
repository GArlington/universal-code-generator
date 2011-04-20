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
//  CCMLTableComboBoxDataSource.m
//  CCMLEditor
//
//  Created by Jeffrey Varner on 1/9/11.


#import "TableComboBoxDataSource.h"

@interface TableComboBoxDataSource (hiiden)

- (void)setup;
- (void)updateDataArray:(NSArray *)listOfOptions tableView:(NSTableView *)tableViewPointer;
- (void)tableSelectionChanged:(NSNotification *)notification;
- (void)treeSelectionChanged:(NSNotification *)notification;
- (void)tableSelectionWillChange:(NSNotification *)notification;

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
    int number_of_items = [[self dataArray] count];
    
    return number_of_items;
}

#pragma mark -------------------------------------------
#pragma mark - Tree/Table xml methods (populate list) -
#pragma mark -------------------------------------------
- (void)tableSelectionWillChange:(NSNotification *)notification
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
        
        // Force a click?
        [view performClickOnCellAtColumn:0 row:rowIndex];
    }

}
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
        
        // Force a click?
        [view performClickOnCellAtColumn:0 row:rowIndex];
        
        // Enable the cell -
        // NSComboBoxCell *cell = (NSComboBoxCell *)[view preparedCellAtColumn:1 row:rowIndex];
        // [cell setEnabled:YES];
        
        // There *must* be a totaly better way to do this ...
	
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
        
        else if ([notSureWhatThisIs isEqualToString:@"steady_state"])
        {
            // Clear -
            [[self dataArray] removeAllObjects];
            
            // Add YES/NO -
            [[self dataArray] addObject:@"YES"];
            [[self dataArray] addObject:@"NO"];
        }
        
        else if ([notSureWhatThisIs isEqualToString:@"required"])
        {
            // Clear -
            [[self dataArray] removeAllObjects];
            
            // Add YES/NO -
            [[self dataArray] addObject:@"YES"];
            [[self dataArray] addObject:@"NO"];
        }
        
        // Ok, these blocks of code deal with graphviz options -
        else if ([notSureWhatThisIs isEqual:@"edge_color"])
        {
            // Ok, so If I get here, then I have a compartment key - we need to get the root document -
			NSXMLDocument *document = [[self selectedXMLNode] rootDocument];
			
			// Run XPath on this bitch to figure out what the available keys are ...
			NSMutableString *strXPath = [[NSMutableString alloc] initWithCapacity:140];
			[strXPath appendString:@".//ListOfEdgeColors/edge_color/@symbol"];
            
            // populate the array with the current data -
			NSError *err=nil; 
			NSArray *listOfOptions = [document nodesForXPath:strXPath error:&err];
            [self updateDataArray:listOfOptions tableView:view];
            
            // cleanup -
            [strXPath release];
        }
        else if ([notSureWhatThisIs isEqual:@"edge_style"])
        {
            // Ok, so If I get here, then I have a compartment key - we need to get the root document -
			NSXMLDocument *document = [[self selectedXMLNode] rootDocument];
			
			// Run XPath on this bitch to figure out what the available keys are ...
			NSMutableString *strXPath = [[NSMutableString alloc] initWithCapacity:140];
			[strXPath appendString:@".//ListOfEdgeStyles/edge_style/@symbol"];
            
            // populate the array with the current data -
			NSError *err=nil; 
			NSArray *listOfOptions = [document nodesForXPath:strXPath error:&err];
            [self updateDataArray:listOfOptions tableView:view];
            
            // cleanup -
            [strXPath release];
        }
        else if ([notSureWhatThisIs isEqual:@"text_color"])
        {
            // Ok, so If I get here, then I have a compartment key - we need to get the root document -
			NSXMLDocument *document = [[self selectedXMLNode] rootDocument];
			
			// Run XPath on this bitch to figure out what the available keys are ...
			NSMutableString *strXPath = [[NSMutableString alloc] initWithCapacity:140];
			[strXPath appendString:@".//ListOfTextColors/text_color/@symbol"];
            
            // populate the array with the current data -
			NSError *err=nil; 
			NSArray *listOfOptions = [document nodesForXPath:strXPath error:&err];
            [self updateDataArray:listOfOptions tableView:view];
            
            // cleanup -
            [strXPath release];
        }

        else if ([notSureWhatThisIs isEqual:@"node_shape"])
        {
            // Ok, so If I get here, then I have a compartment key - we need to get the root document -
			NSXMLDocument *document = [[self selectedXMLNode] rootDocument];
			
			// Run XPath on this bitch to figure out what the available keys are ...
			NSMutableString *strXPath = [[NSMutableString alloc] initWithCapacity:140];
			[strXPath appendString:@".//ListOfNodeShapes/node_shape/@symbol"];
            
            // populate the array with the current data -
			NSError *err=nil; 
			NSArray *listOfOptions = [document nodesForXPath:strXPath error:&err];
            [self updateDataArray:listOfOptions tableView:view];
            
            // cleanup -
            [strXPath release];
        }

        else if ([notSureWhatThisIs isEqual:@"node_style"])
        {
            // Ok, so If I get here, then I have a compartment key - we need to get the root document -
			NSXMLDocument *document = [[self selectedXMLNode] rootDocument];
			
			// Run XPath on this bitch to figure out what the available keys are ...
			NSMutableString *strXPath = [[NSMutableString alloc] initWithCapacity:140];
			[strXPath appendString:@".//ListOfNodeStyles/node_style/@symbol"];
            
            // populate the array with the current data -
			NSError *err=nil; 
			NSArray *listOfOptions = [document nodesForXPath:strXPath error:&err];
            [self updateDataArray:listOfOptions tableView:view];
            
            // cleanup -
            [strXPath release];
        }

        else if ([notSureWhatThisIs isEqual:@"node_color"])
        {
            // Ok, so If I get here, then I have a compartment key - we need to get the root document -
			NSXMLDocument *document = [[self selectedXMLNode] rootDocument];
			
			// Run XPath on this bitch to figure out what the available keys are ...
			NSMutableString *strXPath = [[NSMutableString alloc] initWithCapacity:140];
			[strXPath appendString:@".//ListOfNodeColors/node_color/@symbol"];
            
            // populate the array with the current data -
			NSError *err=nil; 
			NSArray *listOfOptions = [document nodesForXPath:strXPath error:&err];
            [self updateDataArray:listOfOptions tableView:view];
            
            // cleanup -
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


#pragma mark -------------------------------------------
#pragma mark private helper method to update array -
#pragma mark -------------------------------------------

- (void)updateDataArray:(NSArray *)listOfOptions tableView:(NSTableView *)tableViewPointer
{
    // Remove the data from the array -
    [[self dataArray] removeAllObjects];
    
    // Populate the array with the new data -
    for (NSXMLNode *node in listOfOptions)
    {
        // Ok, **finally** so now we can add this to the dataArray -
        [[self dataArray] addObject:[node stringValue]];
    }
    
    // Ok, so now I need to have the view reLoad the data -
    [tableViewPointer reloadData];

}

@end
