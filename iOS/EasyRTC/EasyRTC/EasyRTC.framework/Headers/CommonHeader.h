//
//  CommonHeader.h
//  venus
//
//  Created by Jac Chen on 2018/8/4.
//  Copyright © 2018 Matrix. All rights reserved.
//

#ifndef CommonHeader_h
#define CommonHeader_h

#define SAFE_PERFORM_ON_MAIN_QUEUE(BLOCK)  {  \
    dispatch_block_t block = BLOCK;  \
        if ([NSThread isMainThread] || [NSOperationQueue.currentQueue isEqual:NSOperationQueue.mainQueue]) { \
            block(); \
        } else { \
            dispatch_async(dispatch_get_main_queue(), block); \
        }}

#define TO_SAFE_STR(STR)        ((STR) ? (STR) : @"")

#endif /* CommonHeader_h */
