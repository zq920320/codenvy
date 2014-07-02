/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */

!function ($) {

    "use strict"; // jshint ;_;


    /* TAB CLASS DEFINITION
     * ==================== */

    var Tab = function (element) {
        this.element = $(element)
    }

    Tab.prototype = {

        constructor: Tab, show: function () {
            var $this = this.element
                , $ul = $this.closest('ul:not(.dropdown-menu)')
                , selector = $this.attr('data-target')
                , previous
                , $target
                , e

            if (!selector) {
                selector = $this.attr('href')
                selector = selector && selector.replace(/.*(?=#[^\s]*$)/, '') //strip for ie7
            }

            if ($this.parent('li').hasClass('active')) return

            previous = $ul.find('.active a').last()[0]

            e = $.Event('show', {
                relatedTarget: previous
            })

            $this.trigger(e)

            if (e.isDefaultPrevented()) return

            $target = $(selector)

            this.activate($this.parent('li'), $ul)
            this.activate($target, $target.parent(), function () {
                $this.trigger({
                    type: 'shown', relatedTarget: previous
                })
            })
        }, activate: function (element, container, callback) {
            var $active = container.find('> .active')
                , transition = callback
                    && $.support.transition
                    && $active.hasClass('fade')

            function next() {
                $active
                    .removeClass('active')
                    .find('> .dropdown-menu > .active')
                    .removeClass('active')

                element.addClass('active')

                if (transition) {
                    element[0].offsetWidth // reflow for transition
                    element.addClass('in')
                } else {
                    element.removeClass('fade')
                }

                if (element.parent('.dropdown-menu')) {
                    element.closest('li.dropdown').addClass('active')
                }

                callback && callback()
            }

            transition ?
                $active.one($.support.transition.end, next) :
                next()

            $active.removeClass('in')
        }
    }


    /* TAB PLUGIN DEFINITION
     * ===================== */

    $.fn.tab = function (option) {
        return this.each(function () {
            var $this = $(this)
                , data = $this.data('tab')
            if (!data) $this.data('tab', (data = new Tab(this)))
            if (typeof option == 'string') data[option]()
        })
    }

    $.fn.tab.Constructor = Tab


    /* TAB DATA-API
     * ============ */

    $(function () {
        $('body').on('click.tab.data-api', '[data-toggle="tab"], [data-toggle="pill"]', function (e) {
            e.preventDefault()
            $(this).tab('show')
        })
    })

}(window.jQuery);