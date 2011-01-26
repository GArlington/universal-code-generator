//
//  CCMLIconModel.h
//  CCMLEditor
//
//  Created by Jeffrey Varner on 1/15/11.
//  Copyright 2011 Chemical and Biomolecular Engineering. All rights reserved.
//

#import <Cocoa/Cocoa.h>


@interface TreeIconModel : NSObject {
	
	@private
	NSMutableDictionary *iconTable;				// Holds an icons for tree -
	NSXMLDocument *xmlDocument;
}

// Method to get the instance -
+ (TreeIconModel *)sharedInstance;

// Setup the properties -
@property (retain) NSMutableDictionary *iconTable;

// Get the icon -
-(NSImage *)getIconForKey:(NSString *)keyName;
-(NSString *)getIconKeyForTagName:(NSString *)tagName;
-(NSImage *)cloneIconForKey:(NSString *)key withSize:(NSSize)size;

@end
