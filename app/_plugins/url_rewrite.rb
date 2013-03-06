module Jekyll
  module URLFilter
    def rewrite_url(input)
        case @context.registers[:site].config['profile']
        when "production"
            input.sub(".html","")
        else
            input
        end
    end
  end
end

Liquid::Template.register_filter(Jekyll::URLFilter)
