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


@interface DDTreeNodeProxy (hidden) 

    // Methods -
    -(void)decodeObject:(NSCoder *)coder;

@end

@implementation DDTreeNodeProxy

// synthesize -
@synthesize coderInstance;
@synthesize xmlNode;

- (id)initWithCoder:(NSCoder *)coder {
    self = [super init];
    if (self) {
        
        NSLog(@"Going to decode ...");
        [self decodeObject:coder];
    }
    return self;
}

-(id)init
{
    self = [super init];
    if (self) {
        
    }
    return self;
}

- (void)dealloc
{
    
    // Deallocate instance variables -
    self.coderInstance = nil;
    self.xmlNode = nil;
    
    // Dealloc my super -
    [super dealloc];
}

- (void)encodeWithCoder:(NSCoder *)encoder
{
    // Ok, so I have an xmlNode - we need to get the attributes and values for this node
    NSLog(@"Starting to encode ...");
    
    // Ok, so we need to get see if this tree node has kids, if so then I need encode those bitchez first ... (oh yea, I went with the "z").
    // All I have to say is ... NO SALID ... NO JUSTICE! 
    
    // Ok, get the attributes for this node -
    NSArray *keyList = [[self xmlNode] attributes];
    NSMutableArray *keyNameArray = [[NSMutableArray alloc] initWithCapacity:10];
    
    for (NSXMLNode *attributeNode in keyList)
    {
        // Ok, get the value -
        NSString *attributeValue = [attributeNode stringValue];
        NSString *attributeName = [attributeNode name];
        
        // Add to keyName array -
        [keyNameArray addObject:attributeName];
        
        // encode this pair -
        [encoder encodeObject:attributeValue forKey:attributeName];
    }
    
    // Ok, so the last thing we need to encode is the list of keys -
    [encoder encodeObject:keyNameArray forKey:@"KEY_NAME_ARRAY"];
    [encoder encodeObject:[[self xmlNode] name] forKey:@"NODE_NAME"];
}

-(void)decodeObject:(NSCoder *)coder
{
    
    NSLog(@"Starting the decode ...");
    
    // Ok, now we are going to decode the object -
    NSArray *keyNameList = [coder decodeObjectForKey:@"KEY_NAME_ARRAY"];
    NSMutableDictionary *attDictionary = [[NSMutableDictionary alloc] initWithCapacity:100];
    
    
    // Create a new xmlNode -
    self.xmlNode = [[NSXMLElement alloc] init];
    
    // Populate the properties of the xmlnode -
    for (NSString *keyName in keyNameList)
    {
        // Get the value 
        NSString *tmpValue = [coder decodeObjectForKey:keyName];
        
        NSLog(@"Trying to decode object %@ = %@",keyName,tmpValue);
        
        // Add the data to the xmlNode -
        [attDictionary setObject:tmpValue forKey:keyName];
    }
    
    // Add attributes to the xmlNode -
    [[self xmlNode] setAttributesAsDictionary:attDictionary];
    
     // Name the node -
    [[self xmlNode] setName:[coder decodeObjectForKey:@"NODE_NAME"]];
    
    // release the dictionary -
    [attDictionary release];
}




@end
