module Jekyll
  module URLFilter
    def rewrite_url(input)
        if @context.registers[:site].config['rewrite_urls']
            input.sub(".html","")
        else
            input
        end
      #{}"http://www.example.com/#{input}?#{Time.now.to_i}"
    end
  end
end

Liquid::Template.register_filter(Jekyll::URLFilter)