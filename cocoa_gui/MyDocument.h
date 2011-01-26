//
//  MyDocument.h
//  Translator
//
//  Created by Jeffrey Varner on 1/20/11.
//  Copyright 2011 Chemical and Biomolecular Engineering. All rights reserved.
//


#import <Cocoa/Cocoa.h>
#import "TranslatorWindowController.h"

@interface MyDocument : NSDocument
{
	@private
	TranslatorWindowController *translatorWindowController;	// Custom window controller -
	
}
@end
