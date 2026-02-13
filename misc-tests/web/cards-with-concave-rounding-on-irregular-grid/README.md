# Cards with concave rounding on irregular grid

A Pen created on CodePen.

Original URL: [https://codepen.io/thebabydino/pen/WbxpKPQ](https://codepen.io/thebabydino/pen/WbxpKPQ).

Cards with box convex and concave roundings around buttons. The so-called "inverted border-radius", which feels like such an unintuitive way of describing this.

The gaps between the cards and the items they wrap around have real transparency, there are  no covers, so we may have any image backdrop behind. The shape of the cards also depends on the shape and size of the items they wrap around, it adapts automatically to changes there - try changing the `font-size` just on the `button` elements and see how the cards' shapes adapt automatically, even though the cards don't know about their `button` siblings' `font-size` (which would make this problem impossible to solve with clipping or masking alone)... but they can still detect the changes in the size of grid rows and columns.

This was made to answer [a question asked on reddit](https://www.reddit.com/r/css/comments/1qfccyi/comment/o04bma7/).

### Like this?

If the work I've been putting out since early 2012 has helped you in any way or you just like it, then please remember that praise doesn't keep me afloat financially... but you can! So please consider supporting my work in one of the following ways if you want me to be able to continue coding:

* being a cool cat 😼🎩 and supporting it monthly on Ko-fi or via a one time donation

[![ko-fi](https://assets.codepen.io/2017/btn_kofi.svg)](https://ko-fi.com/anatudor)

* making a weekly anonymous donation via Liberapay - I'll never know who you are if that's your wish

[![Liberapay](https://assets.codepen.io/2017/btn_liberapay.svg)](https://liberapay.com/anatudor/)

* getting me a chocolate voucher

[![Zotter chocolate](https://assets.codepen.io/2017/zotter.jpg)](https://www.zotter.at/en/online-shop/gifts/gift-vouchers/choco-voucher)

* if you're from outside Europe, becoming a patron on Patreon (don't use it for one time donations or if you're from Europe, we're both getting ripped off)

[![become a patron button](https://assets.codepen.io/2017/btn_patreon.png)](https://www.patreon.com/anatudor)

* or at least sharing this to show the world what can be done with CSS these days

Thank you!