//
//  PropertyWrapper.h
//  CCMLEditor
//
//  Created by Jeffrey Varner on 12/19/10.
//  Copyright 2010 Chemical and Biomolecular Engineering. All rights reserved.
//

#import <Cocoa/Cocoa.h>


@interface XMLAttributeWrapper : NSObject {

	NSString *attribute;
	NSString *value;
}

@property (retain) NSString *attribute;
@property (retain) NSString *value;


@end
