//
//  PropertyWrapper.m
//  CCMLEditor
//
//  Created by Jeffrey Varner on 12/19/10.
//  Copyright 2010 Chemical and Biomolecular Engineering. All rights reserved.
//

#import "XMLAttributeWrapper.h"


@implementation XMLAttributeWrapper

@synthesize attribute;
@synthesize value;


// Deallocate this ...
- (void)dealloc
{
	// Deallocate the labels -
	[attribute release];
	[value release];
	
	// Deallocate super -
	[super dealloc];
}


@end
