//
//  NSXMLElement_Category.h
//  CCMLEditor
//
//  Created by Jeffrey Varner on 12/25/10.
//  Copyright 2010 Chemical and Biomolecular Engineering. All rights reserved.
//

#import <Cocoa/Cocoa.h>
#import "TreeNodeNamingModel.h"

@interface NSXMLElement (NSXMLElement_Category)
	
-(NSString *)displayName;
-(BOOL)isLeaf;
	
@end
