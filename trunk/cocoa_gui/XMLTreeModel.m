//
//  XMLTreeModel.m
//  CCMLEditor
//
//  Created by Jeffrey Varner on 12/18/10.
//  Copyright 2010 Chemical and Biomolecular Engineering. All rights reserved.
//

#import "XMLTreeModel.h"


@implementation XMLTreeModel

// Synthesizers -
@synthesize xmlDocument;

-(id)init
{
	self = [super init];
	if (self)
	{
		//NSLog(@"I've loaded my tree model ...");
	}
	return self;
}

// dealloc -
- (void)dealloc
{
	// Deallocate the labels -
	[xmlDocument release];
	
	// Deallocate super -
	[super dealloc];
}

-(NSString *)queryXMLTreeProperty:(NSString *)strXPath
{
	// Method attributes -
	NSString *returnString=@"";
	
	// Get the 
	[returnString autorelease];
	return returnString;
}




@end
