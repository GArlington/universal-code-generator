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
