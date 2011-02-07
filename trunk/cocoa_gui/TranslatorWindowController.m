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
//  TranslatorWindowController.m
//  Translator
//
//  Created by Jeffrey Varner on 1/20/11.


#import "TranslatorWindowController.h"
#import "NSXMLElement_Category.h"



// Private utility methods -
@interface TranslatorWindowController (hidden)

-(void)setup;
-(void)openPanelDidEnd:(NSOpenPanel *)openPanel returnCode:(int)returnCode contextInfo:(void *)contextInfo;
-(void)createXMLDocumentFromFile:(NSString *)file;
-(void)popupButtonSelected:(NSNotification *)notification;
-(void)treeNodeDataChanged:(NSNotification *)notifcation;
-(void)treeSelectionChanged:(NSNotification *)notification;
-(void)codeGenerationCompleted:(NSNotification *)notification;
-(void)removeTreeNodeAlertEnded:(NSAlert *)alert code:(int)choice context:(void *)v;
-(void)savePanelDidEnd:(NSSavePanel *)savePanel returnCode:(int)returnCode contextInfo:(void *)contextInfo;
-(void)executeCodeGenJob;
-(void)launchCustomSheet;
-(void)checkSpecificationTree;
-(void)alertDidEnd:(NSAlert *)alert returnCode:(NSInteger)returnCode contextInfo:(void *)contextInfo; 
-(void)automaticallyPopulateTreePathData;
-(NSMutableString *)formulateCodeGenArgString;
-(void)populatePathLocation:(NSString *)xpath;


@end



@implementation TranslatorWindowController

// synthesize -
#pragma mark --------------------------------
#pragma mark synthesize block
#pragma mark --------------------------------
@synthesize xmlTreeModel;
@synthesize iconModel;
@synthesize progressWheel;
@synthesize bottomDisplayLabel;
@synthesize strFilePath;
@synthesize popupButton;
@synthesize xmlDocument;
@synthesize selectedXMLNode;
@synthesize saveAsButton;
@synthesize treeView;
@synthesize consoleTextField;
@synthesize codeGeneratorButton;
@synthesize progressIndicator;
@synthesize treeCheckButton;
@synthesize fileTypePopupButton;
@synthesize actionButton;
@synthesize propTableView;
@synthesize customSheetController;


#pragma mark --------------------------------
#pragma mark init and dealloc methods
#pragma mark --------------------------------
-(id) init
{
	// Initialize me ...
	self = [super initWithWindowNibName:@"MyDocument"];
	
	// Ok, so if self is not nil, then call setup -
	if (self!=nil)
	{
		// put setup code here ... or call setup
		[self setup];
	}
	
	// return me -
	return self;
}

-(void)dealloc
{
	// release my notifications -
	[[NSNotificationCenter defaultCenter] removeObserver:self];

	// release my instance variables -
	[xmlTreeModel release];
	[iconModel release];
	[strFilePath release];
	[xmlDocument release];
	[selectedXMLNode release];
    [customSheetController release];
	
	// IBOutlet
	self.bottomDisplayLabel = nil;
	self.progressWheel = nil;
	self.popupButton = nil;
	self.saveAsButton = nil;
	self.treeView = nil;
	self.consoleTextField = nil;
	self.codeGeneratorButton = nil;
	self.progressIndicator = nil;
	self.treeCheckButton = nil;
	self.fileTypePopupButton = nil;
	self.actionButton = nil;
	self.propTableView = nil;
	
	// deallocate the super -
	[super dealloc];
}

// Setup me up ...
-(void)setup
{
	// Set the alpha on the window -
	[[self window] setAlphaValue:0.95];
	
	// initialize the tree model object -
	self.xmlTreeModel = [[XMLTreeModel	alloc] init];
	
	// Initialize the system icons -
	self.iconModel = [TreeIconModel sharedInstance];
	
	// Initialize the custom naming object - THIS IS NOT WORKING YET...
	// [TreeNodeNamingModel sharedInstance];
	
	// Register self for different notifications -	
	[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(treeNodeDataChanged:) name:@"TreeNodeDataChanged" object:nil];
	[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(treeSelectionChanged:) name:NSOutlineViewSelectionDidChangeNotification object:nil];
	[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(codeGenerationCompleted:) name:NSTaskDidTerminateNotification object:nil];
	
	// Load the popup mapping tree -
	NSString* templateName = [[NSBundle mainBundle] pathForResource:@"Templates" ofType:@"xml"];
	NSURL *fileURL = [NSURL fileURLWithPath:templateName];
	NSError *errObject = nil;
	
	// Set the NSXMLDocument reference on me - 
	self.xmlDocument = [[NSXMLDocument alloc] initWithContentsOfURL:fileURL options:NSXMLNodeOptionsNone error:&errObject];
	
	// Set the selected popbutton item -
	[[self popupButton] selectItemAtIndex:1];
	
	// Load 
	[[self consoleTextField] insertText:@"Loaded window controller ...\n"];
	
	// Set some attributes on the progress bar -
	[[self progressIndicator] setIndeterminate:YES];
	[[self progressIndicator] setDisplayedWhenStopped:NO];
	[[self progressIndicator] setUsesThreadedAnimation:YES];
	
	// Ok, so we have a weird issue that I didn't get on my laptop -- the file load button has focus when I first start up
	// this should be the tree..
	[[self window] makeFirstResponder:[self treeView]];
	
	// Ok, so we need to have the window pop-up when I start -
	[[self window] makeKeyAndOrderFront:nil];
    
    // Load the window controller -
    self.customSheetController = [[MyCustomSheetController alloc] initWithWindow:[self window]];
}

#pragma mark --------------------------------
#pragma mark IBAction methods
#pragma mark --------------------------------

-(IBAction)checkTreeCompleteness:(NSButton *)sender
{
	// Ok, so I need to go through the current tree and determine if all the required nodes have text in the attributes -
	[[self consoleTextField] setString:@""];
	[[self consoleTextField] setString:@"Checking the specification tree for completeness ... \n"];
	//[[self consoleTextField] insertText:@"--------------------------------------------------------------------------------- \n"];
	[[self progressIndicator] startAnimation:nil];
	
	// Let's create a timer have it fire in a couple of seconds -
	[NSTimer scheduledTimerWithTimeInterval:1.0 
									 target:self 
								   selector:@selector(checkSpecificationTree) 
								   userInfo:nil 
									repeats:NO];
	
}

// Check the specification tree -
-(void)checkSpecificationTree
{
	// Method attributes -
	NSError *err = nil;
	
	// Ok, so when I get here I need to get the tree and check required -
	NSMutableString *strXPath = [[NSMutableString alloc] initWithCapacity:140];
	[strXPath appendString:@"//*[@required='YES']"];
	 
	// Run the query -
	NSArray *listOfChildren = [[[self xmlTreeModel] xmlDocument] nodesForXPath:strXPath error:&err];
	//NSLog(@"How many kids %d for xpath %@",[listOfChildren count],strXPath);

	// Iterate through the kids and see if both attributs have been filled out -
	int counter = 1;
	for (NSXMLElement *node in listOfChildren)
	{
		// Ok, get the attributes for this node -
		NSArray *attributeArray = [node attributes];
		
		// Go through these attributes and see if they are populated -
		for (NSXMLNode *attrNode in attributeArray)
		{
			// Get the name of string -
			NSMutableString *tmpString = [[NSMutableString alloc] initWithCapacity:140];
			[tmpString appendString:@"("];
			[tmpString appendString:[[NSNumber numberWithInt:counter] stringValue]];
			[tmpString appendString:@")\t"];
			[tmpString appendString:[node name]];
			
			// Ok, check to see if this populated -
			if ([[attrNode stringValue] length]==0)
			{
				// Ok, if I get here then I have an issue - something has a zero length string -
				
				[tmpString appendString:@" has a child "];
				[tmpString appendString:[attrNode name]];
				[tmpString appendString:@" which is empty.\n"];
				[[self consoleTextField] insertText:tmpString];
				//[[self consoleTextField] insertText:@"\n"];
				counter = counter + 1;
			}
			else 
			{
				//[tmpString appendString:@" is OK. \n"];
				//[[self consoleTextField] insertText:tmpString];
			}
			[tmpString release];
		}
	}
	
	 
	// update the GUI -
	//[[self consoleTextField] insertText:@"--------------------------------------------------------------------------------- \n"];
	[[self consoleTextField] insertText:@"\nThere were "];
	[[self consoleTextField] insertText:[[NSNumber numberWithInt:counter-1] stringValue]];
	[[self consoleTextField] insertText:@" errors. \n"];
	
	
	[[self progressIndicator] stopAnimation:nil];
	
	// release local memory -
	[strXPath release];
}

-(void)menuSelectionAction:(NSMenuItem *)sender
{
	// Method attributes -
	NSError *err=nil; 
	
	// Get the selected item -
	NSString *tmpString = [sender title];
	
	if (tmpString!=nil)
	{
		// Ok, so when I get here I have a non-nil string title -
	
		if ([tmpString isEqualToString:@"Load custom specification ..."])
		{
			[self doOpenXMLFile:nil];
		}
		else 
		{
			// Setup the XPath query string -
			NSMutableString *strXPath = [[NSMutableString alloc] initWithCapacity:140];
			
			// Lookup the filename given this choice -
			[strXPath appendString:@".//display[@name=\""];
			[strXPath appendString:tmpString];
			[strXPath appendString:@"\"]/@filename"];
			
			// Get all the children of the current node -
			NSArray *listOfChildren = [[self xmlDocument] nodesForXPath:strXPath error:&err];
			
			// Ok, so I should have a filename to load -
			for (NSXMLElement *node in listOfChildren)
			{
				// Ok, get the file name, and load from the bundle -
				NSString* templateName = [[NSBundle mainBundle] pathForResource:[node stringValue] ofType:@"xml"];
				
				//NSLog(@"TemplateName - %@",templateName);
				
				if (templateName!=nil)
				{
					// Load the tree -
					[self createXMLDocumentFromFile:templateName];
				}
			}
			
			// Release the path -
			[strXPath release];
		}
	}
	
	// Enable the tree check button -
	[[self treeCheckButton] setEnabled:YES];
}

// Adds a node of the same type as the selected node -
-(IBAction)addTreeNode:(NSButton *)sender
{
	// Get my current node and copy it -
	if ([self selectedXMLNode]!=nil)
	{
		NSXMLNode *copy = [[self selectedXMLNode] copy];
	
		// Get my parent node and index -
		NSXMLElement *parent = (NSXMLElement *)[[self selectedXMLNode] parent];
	
		// Add the copy to the end of the list -
		[parent addChild:copy];
	
		// reset the reference -
		self.xmlTreeModel.xmlDocument = [parent rootDocument];	
	
		// Ok, I need to release copy - since I'm sure the parent has a retain -
		[copy release];
	}
    
    // Let's create a timer have it fire in a couple of seconds -
	[NSTimer scheduledTimerWithTimeInterval:1.0 
									 target:self 
								   selector:@selector(launchCustomSheet) 
								   userInfo:nil 
									repeats:NO];

    

}

-(void)launchCustomSheet
{
    // Add the currently selected node to the window controller -
    [[self customSheetController] setSelectedXMLNode:[self selectedXMLNode]];
    
    // Ok, launch the node info -
	[[self customSheetController] showCustomSheet:[self window]];
}

-(IBAction)removeTreeNode:(NSButton *)sender
{
	// This stops from deleting the entire tree, but still is a little funky ...
	if ([self selectedXMLNode]!=nil)
	{
		// Popup the alert panel as a sheet - 
		NSAlert *alertPanel = [NSAlert alertWithMessageText:@"Delete configuration treenode?" 
											  defaultButton:@"Delete"
											alternateButton:@"Cancel" 
												otherButton:nil 
								  informativeTextWithFormat:@"Do you really want to delete the %@ node?",[[self selectedXMLNode] displayName]]; 
		
		// Pop-up that mofo - when the user selects a button, the didEndSelector gets called
		[alertPanel beginSheetModalForWindow:[[self treeView] window] modalDelegate:self didEndSelector:@selector(removeTreeNodeAlertEnded:code:context:) contextInfo:NULL];	
	}
}

-(void)removeTreeNodeAlertEnded:(NSAlert *)alert code:(int)choice	context:(void *)v
{	
	// Check to see what button has been pushed - the default is delete 
	if (choice==NSAlertDefaultReturn)
	{	
		// This stops from deleting the entire tree, but still is a little funky ... fixed the funky w/the notification below
		if ([self selectedXMLNode]!=nil)
		{
			// Get the parent node -
			NSXMLElement *parent = (NSXMLElement *)[[self selectedXMLNode] parent];
			
			// Get my index -
			int myIndex = [[self selectedXMLNode] index];
			
			// Remove me from my parent -
			[parent removeChildAtIndex:myIndex]; 
			
			// reset the reference -
			self.xmlTreeModel.xmlDocument = [parent rootDocument];
			
			// Ok, so the tree should have refreshed - set the selected node -
			self.selectedXMLNode = nil;
			
			// Notfy everyone that the selected node has changed -- this should update the selected node -
			NSNotification *myNotification = [NSNotification notificationWithName:NSOutlineViewSelectionDidChangeNotification object:[self treeView]]; 
			[[NSNotificationQueue defaultQueue] enqueueNotification:myNotification postingStyle:NSPostNow coalesceMask:NSNotificationCoalescingOnName forModes:nil];
		}
	}
}



// Open and load an XML file -
-(IBAction)doOpenXMLFile:(NSButton *)sender
{
	// Update the bottom label -
	[[self bottomDisplayLabel] setStringValue:@"Loading xml file ..."];
	
	// Ok, before I do anything - startup the progress bar -
	[progressWheel startAnimation:nil];
	
	// Get the array of file types and open the panel -
	NSOpenPanel *openPanel = [NSOpenPanel openPanel];
	
	// Configure the panel -
	[openPanel setAllowsMultipleSelection:NO];
	[openPanel setCanChooseDirectories:NO];
	[openPanel setExtensionHidden:NO];
	
	
	// Run the panel as a sheet -
	[openPanel beginSheetForDirectory:NSHomeDirectory() 
								 file:nil 
					   modalForWindow:[self window]
						modalDelegate:self 
					   didEndSelector:@selector(openPanelDidEnd:returnCode:contextInfo:)
						  contextInfo:NULL];
	
}

-(void)openPanelDidEnd:(NSOpenPanel *)openPanel returnCode:(int)returnCode contextInfo:(void *)contextInfo
{
	
	//NSLog(@"WTF? Im in openPanelDidEnd");
	
	// Method attributes -
	// NSArray *pathItems;
	
	// Ok, before I do anything - startup the progress bar -
	[progressWheel startAnimation:nil];
	
	// Ok, check which button was pushed -
	if (returnCode == NSOKButton)
	{
		// Get the file that we are going to load -
		NSArray *filesToOpen = [openPanel filenames];
		
		// How many?
		int COUNT = [filesToOpen count];
		for (int i = 0;i<COUNT;i++)
		{
			// Get the string -
			self.strFilePath = [filesToOpen objectAtIndex:i];
		}
		
		// Set the string for the project -
		// pathItems = [[self strFilePath] componentsSeparatedByString:@"/"];
		//[[self displayProjectTextField] setStringValue:[pathItems lastObject]];
		
		// Load the xml document -
		[self createXMLDocumentFromFile:[self strFilePath]];
	}
	
	// Stop the progressWheel
	[[self bottomDisplayLabel] setStringValue:@"Translator loaded normally. Running ..."];
	[progressWheel stopAnimation:nil];
	
	[[self consoleTextField] insertText:@"Loaded project file ..."];
	
	// Set the enabled state on the code gen button -
	[[self codeGeneratorButton] setEnabled:YES];
	
	// Enable the codeTree check button -
	[[self treeCheckButton] setEnabled:YES];
}


-(IBAction)doSaveXMLFile:(NSButton *)sender
{
	// Methods attributes -
	NSSavePanel *savePanel; 
	
	// Update the bottom label -
	[[self bottomDisplayLabel] setStringValue:@"Saving xml file ..."];
	
	/* create or get the shared instance of NSSavePanel */ 
	savePanel = [NSSavePanel savePanel];
	
	// Set some attributes -
	[savePanel setExtensionHidden:YES];
	[savePanel setHasShadow:YES];
	
	// Run the panel as a sheet -
	[savePanel beginSheetForDirectory:NSHomeDirectory() 
								 file:nil 
					   modalForWindow:[self window]
						modalDelegate:self 
					   didEndSelector:@selector(savePanelDidEnd:returnCode:contextInfo:)
						  contextInfo:NULL];
	
	/* set up new attributes */ 
	//[sp setAccessoryView:newView]; 
	//[sp setRequiredFileType:@"xml"];
	
	/* display the NSSavePanel */ 
	// runResult = [sp runModalForDirectory:NSHomeDirectory() file:@""];
}

-(void)savePanelDidEnd:(NSSavePanel *)savePanel returnCode:(int)returnCode contextInfo:(void *)contextInfo
{
	// Ok, before I do anything - startup the progress bar -
	[progressWheel startAnimation:nil];
	
	// Get a reference to the xmlTree held in the XMLTreeModel -
	NSXMLDocument *xmlTree = [[self xmlTreeModel] xmlDocument];
	
	// Get to the crunchy data center -
	NSData *tmpData = [xmlTree XMLDataWithOptions:NSXMLNodePrettyPrint];
	
	/* if successful, save file under designated name */ 
	if (returnCode == NSOKButton) {
		if (![tmpData writeToFile:[savePanel filename] atomically:YES])
		{
			NSBeep();
		}
	}
	
	// Stop the progressWheel
	[[self progressWheel] stopAnimation:nil];
	[[self bottomDisplayLabel] setStringValue:@"Translator loaded normally. Running ..."];
	
	// Ok, the last thing we need to do is update the title bar and flip the dot -- 
	[[self window] setTitle:[savePanel filename]];
	
	// Flip the dot -
	[[self window] setDocumentEdited:NO];
	
	// Set the enabled state on the code gen button -
	[[self codeGeneratorButton] setEnabled:YES];
}


-(IBAction)runCodeGenerator:(NSButton *)sender
{
	// Running -
	[[self consoleTextField] setString:@"Starting conversion..."];
	[[self consoleTextField] setNeedsDisplay:YES];
	
	// Disable the button -
	[[self codeGeneratorButton] setEnabled:NO];

	// Start the progress indicator - and set the label -
	[[self bottomDisplayLabel] setStringValue:@"Executing code generation job ..."];
	[[self bottomDisplayLabel] setNeedsDisplay:YES];
	[[self progressIndicator] startAnimation:nil];
	
	// Let's create a timer have it fire in a couple of seconds -
	[NSTimer scheduledTimerWithTimeInterval:1.0 
									 target:self 
								   selector:@selector(executeCodeGenJob) 
								   userInfo:nil 
									repeats:NO];
	
}

- (void)alertDidEnd:(NSAlert *)alert returnCode:(NSInteger)returnCode contextInfo:(void *)contextInfo 
{
    if (returnCode == NSAlertFirstButtonReturn) 
	{
		// First, reset the state -
		// Ok, reenable the code gen button -
		[[self codeGeneratorButton] setEnabled:YES];
		
		// Stop the progress indicator -
		[[self progressIndicator] stopAnimation:nil];
		[[self bottomDisplayLabel] setStringValue:@"Translator loaded normally. Running ..."];
		
	}
}

-(NSMutableString *)formulateCodeGenArgString
{
	// Method attributes -
	NSMutableString *argsString = [[NSMutableString alloc] initWithCapacity:140];
	NSMutableString *strXPath = [[NSMutableString alloc] initWithCapacity:140];
	NSError *errObject = nil;
	
	// Ok, formulate thge xpath string -
	[strXPath appendString:@".//ListOfArguments/argument/@symbol"];
	NSArray *listOfArgs = [[[self xmlTreeModel] xmlDocument] nodesForXPath:strXPath error:&errObject];
	for (NSXMLElement *node in listOfArgs)
	{
		// Ok, formulate the path string -
		[argsString appendString:[node stringValue]];
		[argsString appendString:@" "];
	}
	
	NSLog(@"What is my args string? %@",argsString);
	
	// Ok, release the local mem -
	[strXPath release];
	
	// Autorelease the args -
	[argsString autorelease];
	
	// return -
	return argsString;
}

-(void)automaticallyPopulateTreePathData
{
	// Ok, in this method we are going to populate some of the tree path info, using our best guess.
	// Entering all these paths in was a pain, so this should make like easier -
	
	// Ok, so we need to update the 
	// <path required="YES" symbol="UNIVERSAL_SERVER_ROOT_DIRECTORY" path_location=""></path>
	//[self populatePathLocation:@".//path[@symbol=\"UNIVERSAL_SERVER_ROOT_DIRECTORY\"]"];
	//[self populatePathLocation:@".//path[@symbol=\"UNIVERSAL_PLUGINS_JAR_DIRECTORY\"]/@path_location"];
	//[self populatePathLocation:@".//path[@symbol=\"UNIVERSAL_SOURCE_OUTPUT_PATH\"]/@path_location"];
	//[self populatePathLocation:@".//path[@symbol=\"UNIVERSAL_DEBUG_OUTPUT_PATH\"]/@path_location"];
}

-(void)populatePathLocation:(NSString *)xpath
{
	
}

-(void)executeCodeGenJob
{
	// Clear all -
	[[self consoleTextField] setString:@""];
	NSError *errObject = nil;
	
	// Fire up a task and setup the args -
	NSTask *aTask = [[NSTask alloc] init];
	NSPipe *outPipe = [[NSPipe alloc] init];
	NSPipe *errPipe = [[NSPipe alloc] init];
	NSMutableArray *args = [NSMutableArray array];
	NSData *inData = nil;
	NSData *errData = nil;
	NSFileHandle *readHandle = [outPipe fileHandleForReading];
	NSFileHandle *readErrHandle = [errPipe fileHandleForReading];
	NSMutableString *tmpBuffer = [[NSMutableString alloc] initWithCapacity:140];
	
	
	// From the tree model - we need to get the input dir -
	NSMutableString *strXPath =  [[NSMutableString alloc] initWithCapacity:140];
	[strXPath appendString:@".//ListOfPaths/path[@symbol='UNIVERSAL_SERVER_ROOT_DIRECTORY']/@path_location"];
	NSArray *listOfChildren = [[[self xmlTreeModel] xmlDocument] nodesForXPath:strXPath error:&errObject];
	for (NSXMLElement *node in listOfChildren)
	{
		// Get the path -
		NSString *tmpNameString = [node stringValue];
		
				
		if ([tmpNameString length]==0)
		{
			// Ok, so If I get here, then I have *not* the path to the server properly -
			// We need to do 2 things, first, reset the state of the application so I can fix the problem and retry the job
			// and second, fire up an NSAlert telling the user...
			
			// close the file -
			[readHandle closeFile];
			[readErrHandle closeFile];
			
			// release local stuff -
			[aTask release];
			[outPipe release];
			[tmpBuffer release];
			[strXPath release];
			[errPipe release];
			
			// Shut down the animation -
			[[self progressIndicator] stopAnimation:nil];
						
			// First, fire up the Alert (reset the state when the user hits the button)
			
			NSAlert *alert = [[[NSAlert alloc] init] autorelease];
			[alert addButtonWithTitle:@"OK"];
			[alert addButtonWithTitle:@"Cancel"];
			[alert setMessageText:@"An error was encounter when contacting the code generation server."];
			[alert setInformativeText:@"Please check your server settings in the specification tree and try again."];
			[alert setAlertStyle:NSWarningAlertStyle];
			
			// Fire up the alert 
			[alert beginSheetModalForWindow:[self window] 
							  modalDelegate:self didEndSelector:@selector(alertDidEnd:returnCode:contextInfo:) 
								contextInfo:nil];
			
		}
		else
		{
			// Clearout the string - we are going to reuse ..
			[strXPath setString:@""];
			
			[strXPath appendString:tmpNameString];
			[strXPath appendString:@"/ExecuteJob.sh"];
			
			//NSLog(@"What is the launch path? %@",strXPath);
			[aTask setLaunchPath:strXPath];
			
			
			// Populate the arguments -
			
			// Args set in the list of args in gui -
			[args addObject:[self formulateCodeGenArgString]];
			
			// Path to the specification file -
			//[args addObject:[[self window] title]];
			// We need to get the full path from the tree -
			[strXPath setString:@""];
			[strXPath appendString:@".//ListOfPaths/path[@symbol='UNIVERSAL_INPUT_PATH']/@path_location"];
			NSArray *pathStringNodeList = [[[self xmlTreeModel] xmlDocument] nodesForXPath:strXPath error:&errObject];
			[strXPath setString:@""];
			NSXMLElement *pathNode = [pathStringNodeList lastObject];
			
			NSLog(@"What is pathNode %@",[pathNode stringValue]);
			
			if ([[pathNode stringValue] length]!=0)
			{
				// If I get here then I have a non-zero path string -
				[strXPath appendString:[pathNode stringValue]];
				[strXPath appendString:@"/"];
				[strXPath appendString:[[[self window] title] lastPathComponent]];
				[args addObject:strXPath];
				
				// Set the arguments (path to the control file -)
				[aTask setArguments:args];
				[aTask setStandardOutput:outPipe];
				[aTask setStandardError:errPipe];
				[aTask setCurrentDirectoryPath:tmpNameString];
				
				// Ok, so we at least have a 
				[aTask launch];
				
				while ((inData = [readHandle availableData]) && ([inData length]))
				{
					NSString *aString = [[[NSString alloc] initWithData:inData encoding:NSUTF8StringEncoding] autorelease];
					//[tmpBuffer appendString:aString];
					[[self consoleTextField] insertText:aString];
				}
				
				while ((errData = [readErrHandle availableData]) && ([errData length]))
				{
					NSString *errString = [[[NSString alloc] initWithData:errData encoding:NSUTF8StringEncoding] autorelease];
					//[tmpBuffer appendString:errString];
					[[self consoleTextField] insertText:errString];
				}			
				
				// close the file -
				[readHandle closeFile];
				[readErrHandle closeFile];
				
				// release local stuff -
				[aTask release];
				[outPipe release];
				[tmpBuffer release];
				[strXPath release];
				[errPipe release];
			}
			else {
				
				// close the file -
				[readHandle closeFile];
				[readErrHandle closeFile];
				
				// release local stuff -
				[aTask release];
				[outPipe release];
				[tmpBuffer release];
				[strXPath release];
				[errPipe release];
				
				// Shut down the animation -
				[[self progressIndicator] stopAnimation:nil];
				
				// First, fire up the Alert (reset the state when the user hits the button)
				
				NSAlert *alert = [[[NSAlert alloc] init] autorelease];
				[alert addButtonWithTitle:@"OK"];
				[alert addButtonWithTitle:@"Cancel"];
				[alert setMessageText:@"An error was encounter when contacting the code generation server."];
				[alert setInformativeText:@"Please check your path settings in the specification tree and try again."];
				[alert setAlertStyle:NSWarningAlertStyle];
				
				// Fire up the alert 
				[alert beginSheetModalForWindow:[self window] 
								  modalDelegate:self didEndSelector:@selector(alertDidEnd:returnCode:contextInfo:) 
									contextInfo:nil];
		
			}
			
		}
		
	}
	
}

#pragma mark --------------------------------
#pragma mark Create tree methods
#pragma mark --------------------------------
-(void)createXMLDocumentFromData:(NSData *)data
{
	NSError *errObject = nil;
	self.xmlTreeModel.xmlDocument = [[NSXMLDocument alloc] initWithData:data options:NSXMLNodeOptionsNone error:&errObject];
	
	// Send a message to the notification center to let folks know that I've loaded an xml file -
	NSNotification *myNotification = [NSNotification notificationWithName:@"TreeDidLoad" object:nil]; 
	[[NSNotificationQueue defaultQueue] enqueueNotification:myNotification postingStyle:NSPostNow coalesceMask:NSNotificationCoalescingOnName forModes:nil];
	
	// Figure out what combo item has been selected -
	NSMutableString *strXPath = [[NSMutableString alloc] initWithCapacity:140];
	[strXPath appendString:@".//Model/@type"];
	NSArray *listOfChildren = [[[self xmlTreeModel] xmlDocument] nodesForXPath:strXPath error:&errObject];
	
	// Get the type node -
	NSXMLElement *typeNode = (NSXMLElement *)[listOfChildren lastObject];
	
	// OK, so now we need to look the string visible to the user -
	[strXPath setString:@""];
	[strXPath appendString:@".//mapping[@type='"];
	[strXPath appendString:[typeNode stringValue]];
	[strXPath appendString:@"']/display/@name"];
	
	//NSLog(@"What was the xpath %@",strXPath);
	
	NSArray *displayName = [[self xmlDocument] nodesForXPath:strXPath error:&errObject];
	NSXMLElement *displayNode = (NSXMLElement *)[displayName lastObject];
	
	//NSLog(@"What was selected %@",[displayNode stringValue]);
	
	// Ok, so now we need to figure which item is selected -
	[[self fileTypePopupButton] selectItemWithTitle:[displayNode stringValue]];
	
	// release -
	[strXPath release];
	
	// Set the enabled state on the code gen button -
	[[self codeGeneratorButton] setEnabled:YES];
	
	// Enable the codeTree check button -
	[[self treeCheckButton] setEnabled:YES];
	
}

-(void)createXMLDocumentFromFile:(NSString *)file
{
	// Create a URL from the file string -
	NSURL *fileURL = [NSURL fileURLWithPath:file];
	NSError *errObject = nil;
	
	// What is my current dir?
	NSString *myPath = [[NSBundle mainBundle] bundlePath];
	[[self consoleTextField] insertText:myPath];
	[[self consoleTextField] insertText:@"\n"];
	
	// Set the NSXMLDocument reference on the tree model 
	self.xmlTreeModel.xmlDocument = [[NSXMLDocument alloc] initWithContentsOfURL:fileURL options:NSXMLNodeOptionsNone error:&errObject];
	
	// Send a message to the notification center to let folks know that I've loaded an xml file -
	NSNotification *myNotification = [NSNotification notificationWithName:@"TreeDidLoad" object:nil]; 
	[[NSNotificationQueue defaultQueue] enqueueNotification:myNotification postingStyle:NSPostNow coalesceMask:NSNotificationCoalescingOnName forModes:nil];
	
	// ---- Calls to setup other GUI features ------ //
	// ok - set the window title 
	[[self window] setTitle:file];
	
	// Figure out what combo item has been selected -
	NSMutableString *strXPath = [[NSMutableString alloc] initWithCapacity:140];
	[strXPath appendString:@".//Model/@type"];
	NSArray *listOfChildren = [[[self xmlTreeModel] xmlDocument] nodesForXPath:strXPath error:&errObject];
	
	if ([listOfChildren count]!=0) 
	{
		// Get the type node -
		NSXMLElement *typeNode = (NSXMLElement *)[listOfChildren lastObject];
		
		// OK, so now we need to look the string visible to the user -
		[strXPath setString:@""];
		[strXPath appendString:@".//mapping[@type='"];
		[strXPath appendString:[typeNode stringValue]];
		[strXPath appendString:@"']/display/@name"];
		
		//NSLog(@"What was the xpath %@",strXPath);
		
		NSArray *displayName = [[self xmlDocument] nodesForXPath:strXPath error:&errObject];
		NSXMLElement *displayNode = (NSXMLElement *)[displayName lastObject];
		
		//NSLog(@"What was selected %@",[displayNode stringValue]);
		
		// Ok, so now we need to figure which item is selected -
		[[self fileTypePopupButton] selectItemWithTitle:[displayNode stringValue]];
		
	}
	else {
		// If I get here, then I have a "custom" file that is a generic xml file type -
		[[self fileTypePopupButton] selectItemWithTitle:@"Load custom specification ..."]; 
	}

	
		
	// release -
	[strXPath release];
}



#pragma mark --------------------------------
#pragma mark Notification methods
#pragma mark --------------------------------
-(void)popupButtonSelected:(NSNotification *)notification
{

	
	
	// Ok, this has a bug -- it runs the query *before* the use has selected ... 
	//NSString *tmpString = [[self popupButton] titleOfSelectedItem];
	
	
}

-(void)codeGenerationCompleted:(NSNotification *)notification
{
	// Ok, reenable the code gen button -
	[[self codeGeneratorButton] setEnabled:YES];
	
	// Stop the progress indicator -
	[[self progressIndicator] stopAnimation:nil];
	[[self bottomDisplayLabel] setStringValue:@"Translator loaded normally. Running ..."];
	
}

-(void)treeNodeDataChanged:(NSNotification *)notifcation
{
	// Ok, grab the window and set the documented edited flag -
	[[self window] setDocumentEdited:YES];
	
	// Ok, the ccmlTree did load. We need to change the enabled status of the save button -
	[[self saveAsButton] setEnabled:YES];
	
	// Update the tree reference -
	if ([self selectedXMLNode]!=nil)
	{
		self.xmlTreeModel.xmlDocument = [[self selectedXMLNode] rootDocument];
	}
}

-(void)treeSelectionChanged:(NSNotification *)notification
{
	// Ok, so when I get here the tree selection has changed -
	
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
			self.selectedXMLNode = (NSXMLElement *)[node representedObject];
		}
	}
}
@end
