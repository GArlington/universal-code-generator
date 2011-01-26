//
//  XMLTreeModel.h
//  CCMLEditor
//
//  Created by Jeffrey Varner on 12/18/10.
//  Copyright 2010 Chemical and Biomolecular Engineering. All rights reserved.
//

#import <Cocoa/Cocoa.h>


@interface XMLTreeModel : NSObject {
	
@private
	NSXMLDocument *xmlDocument;						// Pointer to the xml document model 
}


@property (retain) NSXMLDocument *xmlDocument;

// Method to query the tree - returns back a single string
-(NSString *)queryXMLTreeProperty:(NSString *)strXPath;



@end
