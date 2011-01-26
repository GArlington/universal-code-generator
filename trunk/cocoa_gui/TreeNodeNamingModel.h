//
//  TreeNodeNamingModel.h
//  Translator
//
//  Created by Jeffrey Varner on 1/23/11.
//  Copyright 2011 Chemical and Biomolecular Engineering. All rights reserved.
//

#import <Cocoa/Cocoa.h>


@interface TreeNodeNamingModel : NSObject {

	@private
	NSXMLDocument *xmlDocument;
	
}

// Method to get the instance -
+ (TreeNodeNamingModel *)sharedInstance;


// Properties -
@property (retain) NSXMLDocument *xmlDocument;

// Methods -
-(NSString *)getNameAttributeForTag:(NSString *)tag_name;

@end
