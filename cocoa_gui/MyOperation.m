//
//  MyOperation.m
//  EvaluateThreeGeneModelPartition
//
//  Created by Jeffrey Varner on 5/18/11.
//  Copyright 2011 Chemical and Biomolecular Engineering. All rights reserved.
//

#import "MyOperation.h"


@implementation MyOperation

@synthesize argsArray;
@synthesize pathToExecutable;

- (id)init
{
    self = [super init];
    if (self) {
        // Initialization code here.
        executing = NO;
        finished = NO;
        
        // Set my priority -
        [self setQueuePriority:NSOperationQueuePriorityVeryHigh];
        [self setThreadPriority:1.0];
    }
    
    return self;
}

- (void)dealloc
{
    // Deallocate my mmebers -
    self.argsArray = nil;
    self.pathToExecutable = nil;
    
    // Deallocate my parents -
    [super dealloc];
}


- (BOOL)isConcurrent { return YES; }
- (BOOL)isExecuting { return executing; }
- (BOOL)isFinished { return finished; }
- (int)getCalculation { return calculation; }

- (void)main
{
    // Create an autorelease pool -
    NSAutoreleasePool* pool = [[NSAutoreleasePool alloc] init];
    
    // Do the main work of the operation here.
    NSTask *tmpTask = [[[NSTask alloc] init] autorelease];
   
    // Set the path to the executable -
    [tmpTask setLaunchPath:[self pathToExecutable]];
    
    // Set the arguments array -
    [tmpTask setArguments:[self argsArray]];

    [tmpTask launch];
    [tmpTask waitUntilExit];
    
    //NSLog(@"Monkey butt....");
    
    [self completeOperation];
    [pool release];
}

/*
- (void)start
{
    NSAutoreleasePool* pool = [[NSAutoreleasePool alloc] init];
    
    // Always check for cancellation before launching the task.
    if ([self isCancelled])
    {
        // Must move the operation to the finished state if it is canceled.
        [self willChangeValueForKey:@"isFinished"];
        finished = YES;
        [self didChangeValueForKey:@"isFinished"];
        return;
    }
    
    // If the operation is not canceled, begin executing the task.
    [self willChangeValueForKey:@"isExecuting"];
    [NSThread detachNewThreadSelector:@selector(main) toTarget:self withObject:nil];
    executing = YES;
    [self didChangeValueForKey:@"isExecuting"];
    
    //NSLog(@"Monkey butt....start");
    
    [pool release];
}*/

- (void)completeOperation
{
    [self willChangeValueForKey:@"isFinished"];
    [self willChangeValueForKey:@"isExecuting"];
    
    executing = NO;
    finished = YES;
    
    [self didChangeValueForKey:@"isExecuting"];
    [self didChangeValueForKey:@"isFinished"];
}

@end
