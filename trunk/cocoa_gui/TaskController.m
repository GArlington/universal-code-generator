//
//  TaskController.m
//  Translator
//
//  Created by Jeffrey Varner on 1/20/11.
//  Copyright 2011 Chemical and Biomolecular Engineering. All rights reserved.
//

#import "TaskController.h"


@implementation TaskController

#pragma mark -----------------------------------------
#pragma mark init, dealloc and setup
#pragma mark -----------------------------------------

-(id) init
{
	self = [super init];
	if (self!=nil)
	{
		// Ok, setup the object -
	}
	return self;
}

-(void)dealloc
{
	// Release my instance variables -
	
	
	// call dealloc on my super class -
	[super dealloc];
}

// Translates whatever to whatever - this calls out to Java ...
-(IBAction)runTranslationTask:(NSButton *)sender
{
}

@end
