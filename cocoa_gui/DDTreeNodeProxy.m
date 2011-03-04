// Copyright (c) 2011 Varner Lab
// Chemical and Biomolecular Engineering,
// Cornell University

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
//  DDTreeNodeProxy.m
//  Translator
//
//  Created by Jeffrey Varner on 2/26/11.
//

#import "DDTreeNodeProxy.h"
#import "NSXMLElement_Category.h"


@interface DDTreeNodeProxy (hidden) 

    // Methods -
    -(void)decodeObject:(NSCoder *)coder;
    -(void)setup;
    -(NSString*) stringWithUUID; 

@end

@implementation DDTreeNodeProxy

// synthesize -
@synthesize coderInstance;
@synthesize xmlNode;
@synthesize parent;
@synthesize children;

- (id)initWithCoder:(NSCoder *)coder {
    self = [super init];
    if (self) {
        
        // Set up the object -
        [self setup];
        
        // Decode -
        [self decodeObject:coder];
    }
    return self;
}

-(id)init
{
    self = [super init];
    if (self) {
        
        // Setup -
        [self setup];
    }
    return self;
}

- (void)dealloc
{
    
    // Deallocate instance variables -
    self.coderInstance = nil;
    self.xmlNode = nil;
    self.parent = nil;
    self.children = nil;
    
    // Dealloc my super -
    [super dealloc];
}

-(void)setup
{
    // Setup the children array -
    self.children = [[[NSMutableArray alloc] initWithCapacity:10] autorelease];
}

-(BOOL)isLeaf
{
    BOOL flag = YES;
    
    if ([[self children] count]!=0)
    {
        flag = NO;
    }
    else
    {
        flag = YES;
    }
    
    return flag;
}

- (void)encodeWithCoder:(NSCoder *)encoder
{
    // Ok, so I have an xmlNode - we need to get the attributes and values for this node
    
    // Ok, so we need to get see if this tree node has kids, if so then I need encode those bitchez first ... (oh yea, I went with the "z").
    // All I have to say is ... NO SALAD ... NO JUSTICE! 
       
    // Ok, so If I get here, then my xmlNode has children -
    [encoder encodeObject:[[self xmlNode] XMLString] forKey:@"XML_BLOCK"];
}

-(void)decodeObject:(NSCoder *)coder
{
    
    // Ok, so we need to add the displayName to keys -
    // NSString *nodeDisplayName = [[self xmlNode] displayName];
    
    if ([coder containsValueForKey:@"XML_BLOCK"])
    {
        // Ok, so when I get here I don't just have a simple node. In fact I have a form of the xml that I copied -
        NSString *tmpXMLString = [coder decodeObjectForKey:@"XML_BLOCK"];
        
        // Ok, so I need to create a new XMLElement node -
        NSXMLElement *tmpNode = [[[NSXMLElement alloc] initWithXMLString:tmpXMLString error:nil] autorelease];
        
        // Ok, setup the node to the curent node
        self.xmlNode = tmpNode;
    }
}

-(void)addChild:(DDTreeNodeProxy *)child
{
    // set the child -
    [[self children] addObject:child];
}

-(void)removeAllChildren
{
    [[self children] removeAllObjects];
}

-(NSString*) stringWithUUID 
{
    CFUUIDRef	uuidObj = CFUUIDCreate(nil);//create a new UUID
    //get the string representation of the UUID
    NSString	*uuidString = (NSString*)CFUUIDCreateString(nil, uuidObj);
    CFRelease(uuidObj);
    return [uuidString autorelease];
}

@end
