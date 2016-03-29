# -*- mode: ruby -*-
# vi: set ft=ruby :

# you're doing.
Vagrant.configure(2) do |config|
  # Every Vagrant development environment requires a box. You can search for
  # boxes at https://atlas.hashicorp.com/search.
  # config.vm.box_check_update = false


  config.vm.define "redis" do |redis|
    redis.vm.box = "vivid64"
    redis.vm.network :forwarded_port, guest: 6379, host: 6379
    redis.vm.provider 'virtualbox' do |v|
      v.memory = 2024
      v.cpus = 2
      v.name = "redis"
    end
    redis.vm.provision :shell, :path => "redis/init.sh"
  end

  config.vm.define "web" do |web|
    web.vm.box = 'vivid64'
    web.vm.network 'forwarded_port', guest: 9300, host: 9300
    web.vm.network 'forwarded_port', guest: 9200, host: 9200
    web.vm.network 'forwarded_port', guest: 5432, host: 5432
    web.vm.network 'forwarded_port', guest: 8065, host: 8065
    web.vm.provider 'virtualbox' do |v|
      v.memory = 512
      v.cpus = 2
      v.name = "web"
    end
  end

  # Create a private network, which allows host-only access to the machine
  # using a specific IP.
  # config.vm.network "private_network", ip: "192.168.33.10"

  # Create a public network, which generally matched to bridged network.
  # Bridged networks make the machine appear as another physical device on
  # your network.
  # config.vm.network "public_network"

  # Share an additional folder to the guest VM. The first argument is
  # the path on the host to the actual folder. The second argument is
  # the path on the guest to mount the folder. And the optional third
  # argument is a set of non-required options.
  # config.vm.synced_folder "../data", "/vagrant_data"

  # Provider-specific configuration so you can fine-tune various
  # backing providers for Vagrant. These expose provider-specific options.
  # Example for VirtualBox:
  #
  # config.vm.provider "virtualbox" do |vb|
  #   # Display the VirtualBox GUI when booting the machine
  #   vb.gui = true
  #
  #   # Customize the amount of memory on the VM:
  #   vb.memory = "1024"
  # end
  #
  # View the documentation for the provider you are using for more
  # information on available options.

  # Define a Vagrant Push strategy for pushing to Atlas. Other push strategies
  # such as FTP and Heroku are also available. See the documentation at
  # https://docs.vagrantup.com/v2/push/atlas.html for more information.
  # config.push.define "atlas" do |push|
  #   push.app = "YOUR_ATLAS_USERNAME/YOUR_APPLICATION_NAME"
  # end

  # Enable provisioning with a shell script. Additional provisioners such as
  # Puppet, Chef, Ansible, Salt, and Docker are also available. Please see the
  # documentation for more information about their specific syntax and use.
  # config.vm.provision "shell", inline: <<-SHELL
  #   sudo apt-get update
  #   sudo apt-get install -y apache2
  # SHELL
end
