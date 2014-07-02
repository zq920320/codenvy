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


    /* BUTTON PUBLIC CLASS DEFINITION
     * ============================== */

    var Button = function (element, options) {
        this.$element = $(element)
        this.options = $.extend({}, $.fn.button.defaults, options)
    }

    Button.prototype.setState = function (state) {
        var d = 'disabled'
            , $el = this.$element
            , data = $el.data()
            , val = $el.is('input') ? 'val' : 'html'

        state = state + 'Text'
        data.resetText || $el.data('resetText', $el[val]())

        $el[val](data[state] || this.options[state])

        // push to event loop to allow forms to submit
        setTimeout(function () {
            state == 'loadingText' ?
                $el.addClass(d).attr(d, d) :
                $el.removeClass(d).removeAttr(d)
        }, 0)
    }

    Button.prototype.toggle = function () {
        var $parent = this.$element.parent('[data-toggle="buttons-radio"]')

        $parent && $parent
            .find('.active')
            .removeClass('active')

        this.$element.toggleClass('active')
    }


    /* BUTTON PLUGIN DEFINITION
     * ======================== */

    $.fn.button = function (option) {
        return this.each(function () {
            var $this = $(this)
                , data = $this.data('button')
                , options = typeof option == 'object' && option
            if (!data) $this.data('button', (data = new Button(this, options)))
            if (option == 'toggle') data.toggle()
            else if (option) data.setState(option)
        })
    }

    $.fn.button.defaults = {
        loadingText: 'loading...'
    }

    $.fn.button.Constructor = Button


    /* BUTTON DATA-API
     * =============== */

    $(function () {
        $('body').on('click.button.data-api', '[data-toggle^=button]', function (e) {
            var $btn = $(e.target)
            if (!$btn.hasClass('btn')) $btn = $btn.closest('.btn')
            $btn.button('toggle')
        })
    })

}(window.jQuery);