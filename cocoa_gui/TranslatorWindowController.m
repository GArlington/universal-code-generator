//
//  TranslatorWindowController.m
//  Translator
//
//  Created by Jeffrey Varner on 1/20/11.
//  Copyright 2011 Chemical and Biomolecular Engineering. All rights reserved.
//

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
-(void)removeTreeNodeAlertEnded:(NSAlert *)alert code:(int)choice context:(void *)v;
-(void)savePanelDidEnd:(NSSavePanel *)savePanel returnCode:(int)returnCode contextInfo:(void *)contextInfo;


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
	
	// IBOutlet
	self.bottomDisplayLabel = nil;
	self.progressWheel = nil;
	self.popupButton = nil;
	self.saveAsButton = nil;
	self.treeView = nil;
	self.consoleTextField = nil;
	self.codeGeneratorButton = nil;
	
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
	//[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(popupButtonSelected:) name:NSPopUpButtonWillPopUpNotification object:nil];
	[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(treeNodeDataChanged:) name:@"TreeNodeDataChanged" object:nil];
	[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(treeSelectionChanged:) name:NSOutlineViewSelectionDidChangeNotification object:nil];
	
	// Load the popup mapping tree -
	NSString* templateName = [[NSBundle mainBundle] pathForResource:@"Templates" ofType:@"xml"];
	NSURL *fileURL = [NSURL fileURLWithPath:templateName];
	NSError *errObject = nil;
	
	// Set the NSXMLDocument reference on me - 
	self.xmlDocument = [[NSXMLDocument alloc] initWithContentsOfURL:fileURL options:NSXMLNodeOptionsNone error:&errObject];
	
	// Set the selected popbutton item -
	[[self popupButton] selectItemAtIndex:1];
	
	// Load 
	[[self consoleTextField] setString:@"Loaded window controller ..."];
	
	// Set the window text -
	//[[self window] setTitle:@"UNIVERSAL v1.1"];
}

#pragma mark --------------------------------
#pragma mark IBAction methods
#pragma mark --------------------------------

-(void)menuSelectionAction:(NSMenuItem *)sender
{
	// Method attributes -
	NSError *err=nil; 
	
	// Get the selected item -
	NSString *tmpString = [sender title];
	
	if (tmpString!=nil)
	{
		// Ok, so when I get here I have a non-nil string title -
		
		// Setup the XPath query string -
		NSMutableString *strXPath = [[NSMutableString alloc] initWithCapacity:140];
		
		// Lookup the filename given this choice -
		[strXPath appendString:@".//display[@name=\""];
		[strXPath appendString:tmpString];
		[strXPath appendString:@"\"]/@filename"];
		
		// NSLog(@"XPATH %@",strXPath);
		
		// Get all the children of the current node -
		NSArray *listOfChildren = [[self xmlDocument] nodesForXPath:strXPath error:&err];
		
		// Ok, so I should have a filename to load -
		for (NSXMLElement *node in listOfChildren)
		{
			// Ok, get the file name, and load from the bundle -
			NSString* templateName = [[NSBundle mainBundle] pathForResource:[node stringValue] ofType:@"xml"];
			
			// NSLog(@"TemplateName - %@",templateName);
			
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

// Adds a node of the same type as the selected node -
-(IBAction)addTreeNode:(NSButton *)sender
{
	// Ok, so we need to add a node to the tree -
	
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
	
	[[self consoleTextField] setString:@"Loaded project file ..."];
	
	// Set the enabled state on the code gen button -
	[[self codeGeneratorButton] setEnabled:YES];
}


-(IBAction)doSaveXMLFile:(NSButton *)sender
{
	// Methods attributes -
	NSSavePanel *savePanel; 
	
	// Update the bottom label -
	[[self bottomDisplayLabel] setStringValue:@"Saving xml file ..."];
	
	/* create or get the shared instance of NSSavePanel */ 
	savePanel = [NSSavePanel savePanel];
	
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
		
	// Running -
	[[self consoleTextField] setString:@"Starting conversion..."];
	
	// Need to setup paths -
	[args addObject:[[self window] title]];
	
	// From the tree model - we need to get the input dir -
	NSMutableString *strXPath =  [[NSMutableString alloc] initWithCapacity:140];
	[strXPath appendString:@".//ListOfPaths/path[@symbol='UNIVERSAL_SERVER_ROOT_DIRECTORY']/@path_location"];
	NSArray *listOfChildren = [[[self xmlTreeModel] xmlDocument] nodesForXPath:strXPath error:&errObject];
	for (NSXMLElement *node in listOfChildren)
	{
		// Get the path -
		NSString *tmpNameString = [node stringValue];
		
		// Clearout the string - we are going to reuse ..
		[strXPath setString:@""];
		
		[strXPath appendString:tmpNameString];
		[strXPath appendString:@"/ExecuteJob.sh"];
		
		//NSLog(@"What is the launch path? %@",strXPath);
		
		// Set the launch path -
		[aTask setLaunchPath:strXPath];
		
		// Set the arguments (path to the control file -)
		[aTask setArguments:args];
		[aTask setStandardOutput:outPipe];
		[aTask setStandardError:errPipe];
		[aTask setCurrentDirectoryPath:tmpNameString];
		[aTask launch];
		
		
		while ((inData = [readHandle availableData]) && ([inData length]))
		{
			NSString *aString = [[[NSString alloc] initWithData:inData encoding:NSUTF8StringEncoding] autorelease];
			[tmpBuffer appendString:aString];
		}
		
		while ((errData = [readErrHandle availableData]) && ([errData length]))
		{
			NSString *errString = [[[NSString alloc] initWithData:errData encoding:NSUTF8StringEncoding] autorelease];
			[tmpBuffer appendString:errString];
		}
		
		// Post to the buffer -
		[[self consoleTextField] setString:tmpBuffer];
		
		// close the file -
		[readHandle closeFile];
		[readErrHandle closeFile];
		
		[tmpBuffer appendString:@"Completed ..."];
		[[self consoleTextField] setString:tmpBuffer];
	}
	
	// release local stuff -
	[aTask release];
	[outPipe release];
	[tmpBuffer release];
	[strXPath release];
	[errPipe release];
}

#pragma mark --------------------------------
#pragma mark Create tree methods
#pragma mark --------------------------------
-(void)createXMLDocumentFromFile:(NSString *)file
{
	// Create a URL from the file string -
	NSURL *fileURL = [NSURL fileURLWithPath:file];
	NSError *errObject = nil;
	
	// Set the NSXMLDocument reference on the tree model 
	self.xmlTreeModel.xmlDocument = [[NSXMLDocument alloc] initWithContentsOfURL:fileURL options:NSXMLNodeOptionsNone error:&errObject];
	
	// Send a message to the notification center to let folks know that I've loaded an xml file -
	NSNotification *myNotification = [NSNotification notificationWithName:@"TreeDidLoad" object:nil]; 
	[[NSNotificationQueue defaultQueue] enqueueNotification:myNotification postingStyle:NSPostNow coalesceMask:NSNotificationCoalescingOnName forModes:nil];
	
	// ---- Calls to setup other GUI features ------ //
	// ok - set the window title 
	[[self window] setTitle:file];
	
	// Figure out what combo item has been selected -
	
}



#pragma mark --------------------------------
#pragma mark Notification methods
#pragma mark --------------------------------
-(void)popupButtonSelected:(NSNotification *)notification
{

	
	
	// Ok, this has a bug -- it runs the query *before* the use has selected ... 
	//NSString *tmpString = [[self popupButton] titleOfSelectedItem];
	
	
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
