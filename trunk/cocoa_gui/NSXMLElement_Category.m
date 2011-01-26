//
//  NSXMLElement_Category.m
//  CCMLEditor
//
//  Created by Jeffrey Varner on 12/25/10.
//  Copyright 2010 Chemical and Biomolecular Engineering. All rights reserved.
//

#import "NSXMLElement_Category.h"


@implementation NSXMLElement (NSXMLElement_Category)

-(NSString *)displayName
{
	// Method attributes 
	NSString *labelText = @"";
	
	// If the node has the symbol attribute, append that to the name with a [<symbol>]
	// There has to be away to make this dyanmic and configurable 
	
	if ([self kind]==NSXMLElementKind && [self kind]!= NSXMLCommentKind)
	{
		
		// If I get here then I have an element node - recast and grab the attributes -
		NSXMLElement *tmpElementNode = (NSXMLElement *)self;
		
		/*
		// Grab NodeNaming object -
		TreeNodeNamingModel *namingModel = [TreeNodeNamingModel sharedInstance];
		NSString *tmpNameString = [namingModel getNameAttributeForTag:[self name]];
		if (tmpNameString!=nil)
		{
			labelText = [labelText stringByAppendingString:[self name]];
			labelText = [labelText stringByAppendingString:@" ( "];
			labelText = [labelText stringByAppendingString:tmpNameString];
			labelText = [labelText stringByAppendingString:@" )"];
		}
		 */
		
		// Get attribute name --
		NSXMLNode *attributeNode = [tmpElementNode attributeForName:@"symbol"];
		NSXMLNode *keyNameNode = [tmpElementNode attributeForName:@"filename"];
		NSXMLNode *nameNode = [tmpElementNode attributeForName:@"classname"];
		NSXMLNode *inputClassNameNode = [tmpElementNode attributeForName:@"input_classname"];
		NSXMLNode *outputClassNameNode = [tmpElementNode attributeForName:@"output_classname"];
		
		if (attributeNode!=nil)
		{
			// Ok, If I get here I have a symbol attribute - create a new string with the name and the symbol
			labelText = [labelText stringByAppendingString:[self name]];
			labelText = [labelText stringByAppendingString:@" ( "];
			labelText = [labelText stringByAppendingString:[attributeNode stringValue]];
			labelText = [labelText stringByAppendingString:@" )"];
		}
		else if (keyNameNode!=nil)
		{
			// Ok, If I get here I have a keyname attribute - create a new string with the name and the keyname
			labelText = [labelText stringByAppendingString:[self name]];
			labelText = [labelText stringByAppendingString:@" ( "];
			labelText = [labelText stringByAppendingString:[keyNameNode stringValue]];
			labelText = [labelText stringByAppendingString:@" )"];
		}
		else if (nameNode!=nil)
		{
			// Ok, If I get here I have a keyname attribute - create a new string with the name and the keyname
			labelText = [labelText stringByAppendingString:[self name]];
			labelText = [labelText stringByAppendingString:@" ( "];
			labelText = [labelText stringByAppendingString:[nameNode stringValue]];
			labelText = [labelText stringByAppendingString:@" )"];
		}
		
		else if (inputClassNameNode!=nil)
		{
			// Ok, If I get here I have a keyname attribute - create a new string with the name and the keyname
			labelText = [labelText stringByAppendingString:[self name]];
			labelText = [labelText stringByAppendingString:@" ( "];
			labelText = [labelText stringByAppendingString:[inputClassNameNode stringValue]];
			labelText = [labelText stringByAppendingString:@" )"];
		}
		
		else if (outputClassNameNode!=nil)
		{
			// Ok, If I get here I have a keyname attribute - create a new string with the name and the keyname
			labelText = [labelText stringByAppendingString:[self name]];
			labelText = [labelText stringByAppendingString:@" ( "];
			labelText = [labelText stringByAppendingString:[outputClassNameNode stringValue]];
			labelText = [labelText stringByAppendingString:@" )"];
		}
		else {
			labelText = [labelText stringByAppendingString:[self name]];
			if (!labelText)
			{
				labelText = [labelText	stringByAppendingString:[self stringValue]];
			}
		}
	}
	else if ([self kind]!= NSXMLCommentKind)
	{
		labelText = [labelText	stringByAppendingString:[self name]];
		if (!labelText)
		{
			labelText = [labelText	stringByAppendingString:[self stringValue]];
		}
	}
	else {
		labelText = [labelText stringByAppendingString:@"#Comment"];
	}

	
	return labelText;
}

-(BOOL)isLeaf
{
	// return [self kind] == NSXMLTextKind ? YES : NO;
	
	// Method attributes -
	BOOL blnFLAG = YES;
	
	// Get the children of this node -
	NSArray* childList = [self children];
	
	if ([self childCount] != 0)
	{
		// Iterate through the list --
		for (NSXMLNode *tmpNode in childList)
		{
			if ([self kind] !=  NSXMLAttributeKind)
			{
				blnFLAG = NO;
				break;
			}	
			else 
			{
				blnFLAG = YES;
				break;
			}
		}
	}
	else {
		
		// Ok, If I get here then I *potentially* have a leaf -- need to check to see if this creature has "block" in its name -
		// if yes, then this node can have kids, it just hasn't found the right girl/boy to settle down with yet.. 
		
		// Check to see if the string contains "block" in its name -
		NSRange aRange = [[self displayName] rangeOfString:@"block"];
		
		
		if (aRange.location == NSNotFound)
		{
			// Check for listOf -
			NSRange listRange = [[self displayName] rangeOfString:@"listOf"];
			if (listRange.location != NSNotFound)
			{
				blnFLAG = NO;
			}
			
			// Check for regulated expression -
			NSRange regRange = [[self displayName] rangeOfString:@"regulated_"];
			if (regRange.location != NSNotFound)
			{
				blnFLAG = NO;
			}
			
			// Check for regulated expression -
			NSRange complexRange = [[self displayName] rangeOfString:@"complex"];
			if (complexRange.location != NSNotFound)
			{
				blnFLAG = NO;
			}
			
			// Check for regulated expression -
			NSRange interfaceRange = [[self displayName] rangeOfString:@"interface"];
			if (interfaceRange.location != NSNotFound)
			{
				blnFLAG = NO;
			}
			
			// Check for regulated expression -
			NSRange ligandRange = [[self displayName] rangeOfString:@"ligand"];
			if (ligandRange.location != NSNotFound)
			{
				blnFLAG = NO;
			}
		}
		else {
			
			// Ok, I have "block" in my name so I can have kids, I just haven't found that special someone ... hey ladies, how you doin?
			blnFLAG = NO;
		}
	}
	
	// Just in case I have some sort of major malfunction and am not able to complete my mission ...
	return blnFLAG;
}


@end
