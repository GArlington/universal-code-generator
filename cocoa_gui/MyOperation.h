//
//  MyOperation.h
//  EvaluateThreeGeneModelPartition
//
//  Created by Jeffrey Varner on 5/18/11.
//  Copyright 2011 Chemical and Biomolecular Engineering. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface MyOperation : NSOperation {
    
    int calculation;
    
    BOOL executing;
    BOOL finished;
    NSMutableArray *argsArray;
    NSString *pathToExecutable;
    
}

@property (retain) NSMutableArray *argsArray;
@property (retain) NSString *pathToExecutable;

- (void)completeOperation;
- (int)getCalculation;

@end
