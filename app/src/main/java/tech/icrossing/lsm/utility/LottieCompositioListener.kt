/*
 * Copyright (c) 2019. Highlanders LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package tech.icrossing.lsm.utility

import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieListener
import tech.icrossing.lsm.base.LUISSApp

abstract class LottieCompositioListener: LottieListener<LottieComposition> {

    override fun onResult(result: LottieComposition?) {
        LUISSApp.siriComposition = result
    }
}