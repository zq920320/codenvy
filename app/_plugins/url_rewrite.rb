module Jekyll
  module URLFilter
    def rewrite_url(input)
        if @context.registers[:site].config['rewrite_urls']
            input.sub(".html","")
        else
            input
        end
    end
  end
end

Liquid::Template.register_filter(Jekyll::URLFilter)
