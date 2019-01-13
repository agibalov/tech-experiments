#
# Cookbook:: first_cookbook
# Recipe:: default
#
# Copyright:: 2018, The Authors, All Rights Reserved.

file "#{ENV['HOME']}/test.txt" do
  content 'This file was created by Chef!'
  action :create
end

bash 'some dummy bash' do
  code <<-EOH
    echo "hello world"
  EOH
end

log 'message' do
  message 'hey there'
  level :warn
end

file "#{ENV['HOME']}/test.txt" do
  action :delete
end
