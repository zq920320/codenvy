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


    /* DROPDOWN CLASS DEFINITION
     * ========================= */

    var toggle = '[data-toggle="dropdown"]'
        , Dropdown = function (element) {
            var $el = $(element).on('click.dropdown.data-api', this.toggle)
            $('html').on('click.dropdown.data-api', function () {
                $el.parent().removeClass('open')
            })
        }

    Dropdown.prototype = {

        constructor: Dropdown, toggle: function (e) {
            var $this = $(this)
                , $parent
                , selector
                , isActive

            if ($this.is('.disabled, :disabled')) return

            selector = $this.attr('data-target')

            if (!selector) {
                selector = $this.attr('href')
                selector = selector && selector.replace(/.*(?=#[^\s]*$)/, '') //strip for ie7
            }

            $parent = $(selector)
            $parent.length || ($parent = $this.parent())

            isActive = $parent.hasClass('open')

            clearMenus()

            if (!isActive) $parent.toggleClass('open')

            return false
        }

    }

    function clearMenus() {
        $(toggle).parent().removeClass('open')
    }


    /* DROPDOWN PLUGIN DEFINITION
     * ========================== */

    $.fn.dropdown = function (option) {
        return this.each(function () {
            var $this = $(this)
                , data = $this.data('dropdown')
            if (!data) $this.data('dropdown', (data = new Dropdown(this)))
            if (typeof option == 'string') data[option].call($this)
        })
    }

    $.fn.dropdown.Constructor = Dropdown


    /* APPLY TO STANDARD DROPDOWN ELEMENTS
     * =================================== */

    $(function () {
        $('html').on('click.dropdown.data-api', clearMenus)
        $('body')
            .on('click.dropdown', '.dropdown form', function (e) {
                e.stopPropagation()
            })
            .on('click.dropdown.data-api', toggle, Dropdown.prototype.toggle)
    })

}(window.jQuery);