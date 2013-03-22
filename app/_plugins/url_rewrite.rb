module Jekyll
  module URLFilter
    def url(input)
        case @context.registers[:site].config['profile']
        when "production", "stage"
            if input[0] != "/" then
                input = "/" + input
            end
            input.sub(".html","")
        else
            input
        end
    end
  end
end

Liquid::Template.register_filter(Jekyll::URLFilter)
