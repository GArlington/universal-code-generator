//
//  CCMLIconModel.m
//  CCMLEditor
//
//  Created by Jeffrey Varner on 1/15/11.
//  Copyright 2011 Chemical and Biomolecular Engineering. All rights reserved.
//

#import "TreeIconModel.h"

// private setup methods -
@interface TreeIconModel (hidden)

- (void)setup;

@end


@implementation TreeIconModel

// Synthesize -
@synthesize iconTable;

// static variables -
static TreeIconModel *sharedInstance;	// holds a private static version of myself


#pragma mark -----------------------------------------
#pragma mark - init, setup and dealloc methods 
#pragma mark -----------------------------------------

// setup method -
- (void)setup
{
	// Initilize the inconTable -
	self.iconTable = [[NSMutableDictionary alloc] initWithCapacity:10];
		
	// Load the IconMapping file -
	NSString* templateName = [[NSBundle mainBundle] pathForResource:@"IconMapping" ofType:@"xml"];
	
	NSLog(@"templateName %@",templateName);
	
	NSURL *fileURL = [NSURL fileURLWithPath:templateName];
	NSError *errObject = nil;
	
	// Set the NSXMLDocument reference on me - 
	xmlDocument = [[NSXMLDocument alloc] initWithContentsOfURL:fileURL options:NSXMLNodeOptionsNone error:&errObject];
	
	// Get load the icon's and put them in the tree -
	// Query the tree and get the names of the icns -
	NSMutableString *strXPath = [[NSMutableString alloc] initWithCapacity:140];
	[strXPath appendString:@".//icon/@symbol"];	
	
	
	// Get all the children of the current node -
	NSArray *listOfChildren = [xmlDocument nodesForXPath:strXPath error:&errObject];
	
	// Clear out the string so I can reuse -
	[strXPath setString:@""];
	for (NSXMLNode *node in listOfChildren)
	{
		// Get the symbol so I can get the key for the symbol -
		[strXPath appendString:@".//icon[@symbol='"];
		[strXPath appendString:[node stringValue]];
		[strXPath appendString:@"']/@key"];
		
		//NSLog(@"What xpath am I running for the key %@",strXPath);
		
		// Hit the tree to get the key --
		NSArray *listOfKeys = [xmlDocument nodesForXPath:strXPath error:&errObject];
		for (NSXMLNode *keyNode in listOfKeys)
		{
			// Ok, when I get here I have both the name and the key, so I can load the icon and put in my table -
			NSString *tmpString = [[NSBundle mainBundle] pathForResource:[node stringValue] ofType:@"icns"];
			
			//NSLog(@"Im getting %@ for key %@",[node stringValue],[keyNode stringValue]);
			
			// Load the icon -
			[[self iconTable] setValue:[[[NSImage alloc] initWithContentsOfFile:tmpString] autorelease] forKey:[keyNode stringValue]];
		}
		
		// clear out the string and go around again -
		[strXPath setString:@""];
	}
	
	// Ok, when I get here I need to load the "special" icon mapping -
	[strXPath setString:@""];
	[strXPath appendString:@".//mapping/@tag_name"];
	NSArray *listOfTagNames = [xmlDocument nodesForXPath:strXPath error:&errObject];
	[strXPath setString:@""];
	for (NSXMLNode *tagNameNode in listOfTagNames)
	{
		// Ok, I need to get the icon_key -
		[strXPath appendString:@".//mapping[@tag_name='"];
		[strXPath appendString:[tagNameNode stringValue]];
		[strXPath appendString:@"']/@icon_key"];
		NSArray *listOfIconKeys = [xmlDocument nodesForXPath:strXPath error:&errObject];
			
		for (NSXMLNode *iconKeyNode in listOfIconKeys)
		{
			// Put the tag_name and icon_key into the table -
			[[self iconTable] setValue:[iconKeyNode stringValue]
								forKey:[tagNameNode stringValue]];
		}
		
		// clear out the string -
		[strXPath setString:@""];
	}
	
	// Release -
	[strXPath release];
	[xmlDocument release];
}


// initialize me ...
-(id)init
{
	self = [super init];
	if (self!=nil)
	{
		// Ok, initialize me -
		[self setup];
	}
	return self;
}

// deallocate me ...
-(void)dealloc
{
	// deallocate the icon table -
	[iconTable release];
	[xmlDocument release];
	
	// dellocate me ...
	[super dealloc];
}

#pragma mark -----------------------------------------
#pragma mark - static accesor method 
#pragma mark -----------------------------------------

// Static accesor method -
+ (TreeIconModel*)sharedInstance
{
    @synchronized(self)
    {
        if (sharedInstance == nil)
            sharedInstance = [[TreeIconModel alloc] init];
    }
	
    return sharedInstance;
}

#pragma mark -----------------------------------------
#pragma mark -- Icon get and set information --
#pragma mark -----------------------------------------
-(NSImage *)getIconForKey:(NSString *)keyName
{
	return [[self iconTable] objectForKey:keyName];
}

-(NSString *)getIconKeyForTagName:(NSString *)tagName
{
	return [[self iconTable] objectForKey:tagName];
}

-(NSImage *)cloneIconForKey:(NSString *)key withSize:(NSSize)size
{
	// Copy the image -
	NSImage *clonedImage = [[[self iconTable] objectForKey:key] copy];
	
	// set the size -
	[clonedImage setSize:size];
	
	// autorelease -
	[clonedImage autorelease];
	
	// return -
	return clonedImage;
}

@end
