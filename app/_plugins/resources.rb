module Jekyll
  module Resources
    def resource(input)
        if @context.registers[:site].config['yeoman']
            #if running with Yeoman, use /_site prefix
            "/_site" + input
        else
            input
        end
    end
  end
end

Liquid::Template.register_filter(Jekyll::Resources)
