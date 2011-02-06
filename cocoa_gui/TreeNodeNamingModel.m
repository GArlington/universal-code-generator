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
//  TreeNodeNamingModel.m
//  Translator
//
//  Created by Jeffrey Varner on 1/23/11.

#import "TreeNodeNamingModel.h"

// private setup methods -
@interface TreeNodeNamingModel (hidden)

- (void)setup;

@end


@implementation TreeNodeNamingModel

// static variables -
static TreeNodeNamingModel *sharedInstance;	// holds a private static version of myself

#pragma mark -----------------------------------------
#pragma mark - synthesize
#pragma mark -----------------------------------------
@synthesize xmlDocument;

#pragma mark -----------------------------------------
#pragma mark - getInstance, setup and dealloc methods 
#pragma mark -----------------------------------------
// Static accesor method -
+ (TreeNodeNamingModel*)sharedInstance
{
    @synchronized(self)
    {
        if (sharedInstance == nil)
            sharedInstance = [[TreeNodeNamingModel alloc] init];
    }
	
    return sharedInstance;
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
	[xmlDocument release];
	
	// dellocate me ...
	[super dealloc];
}

-(void)setup
{
	// Ok, we need to load the document -
	NSString* templateName = [[NSBundle mainBundle] pathForResource:@"NodeNaming" ofType:@"xml"];
	NSURL *fileURL = [NSURL fileURLWithPath:templateName];
	NSError *errObject = nil;
	
	// Set the NSXMLDocument reference on me - 
	self.xmlDocument = [[NSXMLDocument alloc] initWithContentsOfURL:fileURL options:NSXMLNodeOptionsNone error:&errObject];
}

#pragma mark -----------------------------------------
#pragma mark - Methods to query the tree -
#pragma mark -----------------------------------------
-(NSString *)getNameAttributeForTag:(NSString *)tag_name
{
	// Method attributes -
	NSError *errObject = nil;
	NSString *returnString;
	
	// Ok, so I need to run xpath to get the attribute to display for the tag_name -
	NSMutableString *strXPath = [[NSMutableString alloc] initWithCapacity:140];
	[strXPath appendString:@".//item[@tree_name='"];
	[strXPath appendString:tag_name];
	[strXPath appendString:@"']/@attribute_name"];
	
	NSLog(@"XPATH sent to name model %@",strXPath);
	
	// Get all the children of document w/tag_name -
	NSArray *listOfChildren = [[self xmlDocument] nodesForXPath:strXPath error:&errObject];
	
	if ([listOfChildren count]>0)
	{
		// Get the last element -
		 returnString = [[NSString alloc] initWithString:[listOfChildren lastObject]];
	}
	else
	{
		// Return the name if we can't find the name -
		returnString = nil;
	}
	
	// release the xpath string -
	[strXPath release];
	
	// return string -
	[returnString autorelease];
	return returnString;
}


@end
