-- phpMyAdmin SQL Dump
-- version 2.11.10
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Sep 20, 2012 at 09:45 AM
-- Server version: 5.0.45
-- PHP Version: 4.3.2

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `eta`
--

-- --------------------------------------------------------

--
-- Table structure for table `approved_cluster`
--

DROP TABLE IF EXISTS `approved_cluster`;
CREATE TABLE IF NOT EXISTS `approved_cluster` (
  `id` int(10) NOT NULL auto_increment,
  `request` int(10) NOT NULL,
  `key` varchar(30) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=10 ;

-- --------------------------------------------------------

--
-- Table structure for table `auto_cluster`
--

DROP TABLE IF EXISTS `auto_cluster`;
CREATE TABLE IF NOT EXISTS `auto_cluster` (
  `id` int(10) NOT NULL auto_increment,
  `server` varchar(200) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `external_job`
--

DROP TABLE IF EXISTS `external_job`;
CREATE TABLE IF NOT EXISTS `external_job` (
  `id` int(10) NOT NULL auto_increment,
  `local_job` int(10) NOT NULL,
  `external_job` int(10) NOT NULL,
  `global_cluster` int(10) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=28 ;

-- --------------------------------------------------------

--
-- Table structure for table `external_notifications`
--

DROP TABLE IF EXISTS `external_notifications`;
CREATE TABLE IF NOT EXISTS `external_notifications` (
  `id` int(10) NOT NULL auto_increment,
  `job` int(10) NOT NULL,
  `email` varchar(60) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=392 ;

-- --------------------------------------------------------

--
-- Table structure for table `external_token`
--

DROP TABLE IF EXISTS `external_token`;
CREATE TABLE IF NOT EXISTS `external_token` (
  `id` int(10) NOT NULL auto_increment,
  `token` int(10) NOT NULL,
  `external_token` varchar(30) NOT NULL,
  `site` varchar(200) default ' ',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `ext` (`external_token`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=420 ;

-- --------------------------------------------------------

--
-- Table structure for table `external_wrapper`
--

DROP TABLE IF EXISTS `external_wrapper`;
CREATE TABLE IF NOT EXISTS `external_wrapper` (
  `id` int(10) NOT NULL auto_increment,
  `wrapper` int(10) NOT NULL,
  `site` varchar(100) NOT NULL,
  `key` varchar(30) NOT NULL,
  `queue` varchar(50) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=8 ;

-- --------------------------------------------------------

--
-- Table structure for table `favorite`
--

DROP TABLE IF EXISTS `favorite`;
CREATE TABLE IF NOT EXISTS `favorite` (
  `id` int(11) NOT NULL auto_increment,
  `userid` int(11) NOT NULL,
  `type` int(11) NOT NULL,
  `value` longtext NOT NULL,
  `name` varchar(30) default NULL,
  PRIMARY KEY  (`id`),
  FULLTEXT KEY `value` (`value`,`name`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 AUTO_INCREMENT=117 ;

-- --------------------------------------------------------

--
-- Table structure for table `filetype`
--

DROP TABLE IF EXISTS `filetype`;
CREATE TABLE IF NOT EXISTS `filetype` (
  `id` int(11) NOT NULL auto_increment,
  `type` varchar(100) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `single` (`type`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=145 ;

-- --------------------------------------------------------

--
-- Table structure for table `global_cluster`
--

DROP TABLE IF EXISTS `global_cluster`;
CREATE TABLE IF NOT EXISTS `global_cluster` (
  `id` int(10) NOT NULL auto_increment,
  `address` varchar(200) NOT NULL,
  `key` varchar(30) NOT NULL,
  `type` int(11) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=12 ;

-- --------------------------------------------------------

--
-- Table structure for table `help`
--

DROP TABLE IF EXISTS `help`;
CREATE TABLE IF NOT EXISTS `help` (
  `id` int(10) NOT NULL auto_increment,
  `action` varchar(80) NOT NULL,
  `html` longtext NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `action` (`action`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=5 ;

-- --------------------------------------------------------

--
-- Table structure for table `job`
--

DROP TABLE IF EXISTS `job`;
CREATE TABLE IF NOT EXISTS `job` (
  `id` int(11) NOT NULL auto_increment,
  `name` varchar(90) NOT NULL,
  `user` int(11) NOT NULL,
  `status` varchar(40) NOT NULL,
  `time` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `machine` varchar(100) NOT NULL default 'N/A',
  `wrapper` int(11) NOT NULL,
  `working_dir` varchar(300) NOT NULL,
  `parent` int(11) NOT NULL default '0',
  `submit_time` timestamp NULL default NULL,
  `run_time` timestamp NULL default NULL,
  `finished_time` timestamp NULL default NULL,
  `waiting_for` int(10) NOT NULL,
  `specs` longtext NOT NULL,
  `pipeline` int(11) NOT NULL,
  `public` int(11) NOT NULL default '0',
  `stdoutPath` varchar(200) default NULL,
  `exitCode` int(10) NOT NULL default '0',
  PRIMARY KEY  (`id`),
  KEY `userKey` (`user`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 AUTO_INCREMENT=40717 ;

-- --------------------------------------------------------

--
-- Table structure for table `jobs`
--

DROP TABLE IF EXISTS `jobs`;
CREATE TABLE IF NOT EXISTS `jobs` (
  `id` int(11) NOT NULL auto_increment,
  `user` int(11) NOT NULL,
  `parent` int(11) NOT NULL,
  `job` int(11) NOT NULL,
  `name` varchar(45) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=9645 ;

-- --------------------------------------------------------

--
-- Table structure for table `job_hash`
--

DROP TABLE IF EXISTS `job_hash`;
CREATE TABLE IF NOT EXISTS `job_hash` (
  `id` int(10) NOT NULL auto_increment,
  `job` int(10) NOT NULL,
  `hash` varchar(32) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=6309 ;

-- --------------------------------------------------------

--
-- Table structure for table `job_heap`
--

DROP TABLE IF EXISTS `job_heap`;
CREATE TABLE IF NOT EXISTS `job_heap` (
  `id` int(10) NOT NULL auto_increment,
  `job` int(10) NOT NULL,
  `name` varchar(100) NOT NULL,
  `value` longtext NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=6631 ;

-- --------------------------------------------------------

--
-- Table structure for table `job_note`
--

DROP TABLE IF EXISTS `job_note`;
CREATE TABLE IF NOT EXISTS `job_note` (
  `id` int(11) NOT NULL auto_increment,
  `note` longtext NOT NULL,
  `user` int(11) NOT NULL,
  `date` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `job` int(11) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=14 ;

-- --------------------------------------------------------

--
-- Table structure for table `job_value`
--

DROP TABLE IF EXISTS `job_value`;
CREATE TABLE IF NOT EXISTS `job_value` (
  `id` int(11) NOT NULL auto_increment,
  `job` int(11) NOT NULL,
  `input` int(11) NOT NULL,
  `value` longtext NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=144133 ;

-- --------------------------------------------------------

--
-- Table structure for table `notification`
--

DROP TABLE IF EXISTS `notification`;
CREATE TABLE IF NOT EXISTS `notification` (
  `id` int(10) NOT NULL auto_increment,
  `user` int(10) NOT NULL,
  `job` int(10) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=174 ;

-- --------------------------------------------------------

--
-- Table structure for table `output_hash`
--

DROP TABLE IF EXISTS `output_hash`;
CREATE TABLE IF NOT EXISTS `output_hash` (
  `id` int(10) NOT NULL auto_increment,
  `job` int(10) NOT NULL,
  `path` varchar(400) NOT NULL,
  `hash` varchar(32) NOT NULL,
  `output` int(10) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=9552 ;

-- --------------------------------------------------------

--
-- Table structure for table `pending_cluster`
--

DROP TABLE IF EXISTS `pending_cluster`;
CREATE TABLE IF NOT EXISTS `pending_cluster` (
  `id` int(10) NOT NULL auto_increment,
  `request` varchar(30) NOT NULL,
  `organization` varchar(200) NOT NULL,
  `username` varchar(200) NOT NULL,
  `user` int(10) NOT NULL,
  `email` varchar(200) NOT NULL,
  `status` varchar(100) NOT NULL,
  `type` int(2) NOT NULL,
  `server` varchar(200) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=16 ;

-- --------------------------------------------------------

--
-- Table structure for table `pipeline`
--

DROP TABLE IF EXISTS `pipeline`;
CREATE TABLE IF NOT EXISTS `pipeline` (
  `id` int(11) NOT NULL auto_increment,
  `name` varchar(50) NOT NULL,
  `description` longtext NOT NULL,
  `creator` int(11) NOT NULL,
  `public` tinyint(1) NOT NULL,
  `public_id` int(11) NOT NULL,
  `modified` timestamp NOT NULL default '0000-00-00 00:00:00' on update CURRENT_TIMESTAMP,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=62 ;

-- --------------------------------------------------------

--
-- Table structure for table `pipelines`
--

DROP TABLE IF EXISTS `pipelines`;
CREATE TABLE IF NOT EXISTS `pipelines` (
  `id` int(11) NOT NULL auto_increment,
  `parent` int(11) NOT NULL,
  `user` int(11) NOT NULL,
  `pipeline` int(11) NOT NULL,
  `name` varchar(90) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=25 ;

-- --------------------------------------------------------

--
-- Table structure for table `pipeline_component`
--

DROP TABLE IF EXISTS `pipeline_component`;
CREATE TABLE IF NOT EXISTS `pipeline_component` (
  `id` int(11) NOT NULL auto_increment,
  `wrapper` int(11) NOT NULL,
  `pipeline` int(11) NOT NULL,
  `step` int(11) NOT NULL,
  `job_options` longtext NOT NULL,
  `pipe` int(11) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=204 ;

-- --------------------------------------------------------

--
-- Table structure for table `pipeline_component_values`
--

DROP TABLE IF EXISTS `pipeline_component_values`;
CREATE TABLE IF NOT EXISTS `pipeline_component_values` (
  `id` int(11) NOT NULL auto_increment,
  `pipeline_component` int(11) NOT NULL,
  `input` int(11) NOT NULL,
  `value` longtext NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=5213 ;

-- --------------------------------------------------------

--
-- Table structure for table `pipeline_input`
--

DROP TABLE IF EXISTS `pipeline_input`;
CREATE TABLE IF NOT EXISTS `pipeline_input` (
  `id` int(11) NOT NULL auto_increment,
  `description` longtext,
  `pipeline` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `defaultValue` varchar(200) default NULL,
  `required` tinyint(1) NOT NULL,
  `order` int(11) NOT NULL,
  `type` longtext NOT NULL,
  `displayType` enum('Hidden','Advanced','Default') NOT NULL default 'Default',
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=66 ;

-- --------------------------------------------------------

--
-- Table structure for table `pipeline_output`
--

DROP TABLE IF EXISTS `pipeline_output`;
CREATE TABLE IF NOT EXISTS `pipeline_output` (
  `id` int(11) NOT NULL auto_increment,
  `pipeline` int(11) NOT NULL,
  `name` varchar(45) NOT NULL,
  `description` longtext,
  `value` varchar(100) default NULL,
  `type` varchar(200) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=20 ;

-- --------------------------------------------------------

--
-- Table structure for table `pipeline_star`
--

DROP TABLE IF EXISTS `pipeline_star`;
CREATE TABLE IF NOT EXISTS `pipeline_star` (
  `id` int(11) NOT NULL auto_increment,
  `user` int(11) NOT NULL,
  `pipeline` int(11) NOT NULL,
  `rating` int(11) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `plugin`
--

DROP TABLE IF EXISTS `plugin`;
CREATE TABLE IF NOT EXISTS `plugin` (
  `id` int(11) NOT NULL auto_increment,
  `name` varchar(100) NOT NULL,
  `version` varchar(20) NOT NULL,
  `description` longtext NOT NULL,
  `author` varchar(100) NOT NULL,
  `type` varchar(40) NOT NULL,
  `icon` varchar(100) NOT NULL,
  `identifier` varchar(30) NOT NULL,
  `email` varchar(100) default NULL,
  `index` varchar(100) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=13 ;

-- --------------------------------------------------------

--
-- Table structure for table `plugins`
--

DROP TABLE IF EXISTS `plugins`;
CREATE TABLE IF NOT EXISTS `plugins` (
  `id` int(11) NOT NULL auto_increment,
  `url` varchar(300) NOT NULL,
  `filetype` varchar(30) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=2 ;

-- --------------------------------------------------------

--
-- Table structure for table `plugin_filetype`
--

DROP TABLE IF EXISTS `plugin_filetype`;
CREATE TABLE IF NOT EXISTS `plugin_filetype` (
  `id` int(11) NOT NULL auto_increment,
  `plugin` int(10) NOT NULL,
  `type` varchar(50) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=5 ;

-- --------------------------------------------------------

--
-- Table structure for table `plugin_permission`
--

DROP TABLE IF EXISTS `plugin_permission`;
CREATE TABLE IF NOT EXISTS `plugin_permission` (
  `id` int(10) NOT NULL auto_increment,
  `plugin` int(10) NOT NULL,
  `permission` varchar(100) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=15 ;

-- --------------------------------------------------------

--
-- Table structure for table `public_result`
--

DROP TABLE IF EXISTS `public_result`;
CREATE TABLE IF NOT EXISTS `public_result` (
  `id` int(10) NOT NULL auto_increment,
  `job` int(10) NOT NULL,
  `key` varchar(30) NOT NULL,
  `external_wrapper` int(10) NOT NULL,
  `referer` varchar(300) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=391 ;

-- --------------------------------------------------------

--
-- Table structure for table `request`
--

DROP TABLE IF EXISTS `request`;
CREATE TABLE IF NOT EXISTS `request` (
  `id` int(10) NOT NULL auto_increment,
  `user` int(10) NOT NULL,
  `request` longtext NOT NULL,
  `log` longtext NOT NULL,
  `status` varchar(50) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=39 ;

-- --------------------------------------------------------

--
-- Table structure for table `request_file`
--

DROP TABLE IF EXISTS `request_file`;
CREATE TABLE IF NOT EXISTS `request_file` (
  `id` int(11) NOT NULL auto_increment,
  `request` int(11) NOT NULL,
  `file` varchar(200) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=10 ;

-- --------------------------------------------------------

--
-- Table structure for table `request_item`
--

DROP TABLE IF EXISTS `request_item`;
CREATE TABLE IF NOT EXISTS `request_item` (
  `id` int(10) NOT NULL auto_increment,
  `user` int(10) NOT NULL,
  `date` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `response` longtext NOT NULL,
  `request` int(10) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=191 ;

-- --------------------------------------------------------

--
-- Table structure for table `request_new`
--

DROP TABLE IF EXISTS `request_new`;
CREATE TABLE IF NOT EXISTS `request_new` (
  `id` int(10) NOT NULL auto_increment,
  `status` varchar(20) NOT NULL,
  `reporter` int(10) NOT NULL,
  `timestamp` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `type` varchar(20) NOT NULL,
  `summary` varchar(200) NOT NULL,
  `description` longtext NOT NULL,
  PRIMARY KEY  (`id`),
  FULLTEXT KEY `summary` (`summary`,`description`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=119 ;

-- --------------------------------------------------------

--
-- Table structure for table `request_star`
--

DROP TABLE IF EXISTS `request_star`;
CREATE TABLE IF NOT EXISTS `request_star` (
  `id` int(10) NOT NULL auto_increment,
  `user` int(10) NOT NULL,
  `request` int(10) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=148 ;

-- --------------------------------------------------------

--
-- Table structure for table `result`
--

DROP TABLE IF EXISTS `result`;
CREATE TABLE IF NOT EXISTS `result` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `url` varchar(40) NOT NULL,
  `session` int(11) NOT NULL,
  `public` bit(1) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=23 ;

-- --------------------------------------------------------

--
-- Table structure for table `session`
--

DROP TABLE IF EXISTS `session`;
CREATE TABLE IF NOT EXISTS `session` (
  `id` int(11) NOT NULL auto_increment,
  `creator` int(11) NOT NULL,
  `file` varchar(200) NOT NULL,
  `token` varchar(20) NOT NULL,
  `plugin` int(11) NOT NULL,
  `public` int(1) NOT NULL default '0',
  PRIMARY KEY  (`id`),
  KEY `id` (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=219 ;

-- --------------------------------------------------------

--
-- Table structure for table `shares`
--

DROP TABLE IF EXISTS `shares`;
CREATE TABLE IF NOT EXISTS `shares` (
  `id` int(11) NOT NULL auto_increment,
  `user` int(11) NOT NULL,
  `session` int(11) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=21 ;

-- --------------------------------------------------------

--
-- Table structure for table `token`
--

DROP TABLE IF EXISTS `token`;
CREATE TABLE IF NOT EXISTS `token` (
  `id` int(10) NOT NULL auto_increment,
  `token` varchar(30) NOT NULL,
  `user` int(10) NOT NULL,
  `ip` varchar(20) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `tok` (`token`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=3641 ;

-- --------------------------------------------------------

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
CREATE TABLE IF NOT EXISTS `user` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `username` varchar(41) NOT NULL,
  `name` varchar(50) NOT NULL,
  `password` varchar(41) NOT NULL,
  `permission` tinyint(4) NOT NULL,
  `email` varchar(50) NOT NULL,
  `phone` varchar(20) NOT NULL,
  `byEmail` tinyint(1) NOT NULL default '0',
  `byText` tinyint(1) NOT NULL default '0',
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=207 ;

-- --------------------------------------------------------

--
-- Table structure for table `user_api_connection`
--

DROP TABLE IF EXISTS `user_api_connection`;
CREATE TABLE IF NOT EXISTS `user_api_connection` (
  `token` varchar(30) NOT NULL,
  `cypher` varchar(30) NOT NULL,
  `user` int(10) NOT NULL,
  `lastused` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  PRIMARY KEY  (`token`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `user_cluster`
--

DROP TABLE IF EXISTS `user_cluster`;
CREATE TABLE IF NOT EXISTS `user_cluster` (
  `id` int(10) NOT NULL auto_increment,
  `global` int(10) NOT NULL,
  `key` varchar(30) NOT NULL,
  `user` int(10) NOT NULL,
  `company` varchar(200) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `user_connection`
--

DROP TABLE IF EXISTS `user_connection`;
CREATE TABLE IF NOT EXISTS `user_connection` (
  `token` varchar(30) NOT NULL,
  `user` int(10) NOT NULL,
  PRIMARY KEY  (`token`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `user_setting`
--

DROP TABLE IF EXISTS `user_setting`;
CREATE TABLE IF NOT EXISTS `user_setting` (
  `id` int(11) NOT NULL auto_increment,
  `user` int(11) NOT NULL,
  `setting` varchar(45) NOT NULL,
  `value` varchar(45) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=365 ;

-- --------------------------------------------------------

--
-- Table structure for table `wrapper`
--

DROP TABLE IF EXISTS `wrapper`;
CREATE TABLE IF NOT EXISTS `wrapper` (
  `id` int(11) NOT NULL auto_increment,
  `name` varchar(100) NOT NULL,
  `description` longtext NOT NULL,
  `program` varchar(100) NOT NULL,
  `creator` int(11) NOT NULL,
  `public` tinyint(1) NOT NULL,
  `modified` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `public_id` int(11) default '0',
  PRIMARY KEY  (`id`),
  FULLTEXT KEY `search` (`name`,`description`,`program`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=5941 ;

-- --------------------------------------------------------

--
-- Table structure for table `wrappers`
--

DROP TABLE IF EXISTS `wrappers`;
CREATE TABLE IF NOT EXISTS `wrappers` (
  `id` int(11) NOT NULL auto_increment,
  `parent` int(11) NOT NULL,
  `user` int(11) NOT NULL,
  `wrapper` int(11) NOT NULL,
  `name` varchar(45) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=1802 ;

-- --------------------------------------------------------

--
-- Table structure for table `wrapper_comment`
--

DROP TABLE IF EXISTS `wrapper_comment`;
CREATE TABLE IF NOT EXISTS `wrapper_comment` (
  `id` int(11) NOT NULL auto_increment,
  `user` int(11) NOT NULL,
  `timestamp` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `comment` longtext NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `wrapper_env`
--

DROP TABLE IF EXISTS `wrapper_env`;
CREATE TABLE IF NOT EXISTS `wrapper_env` (
  `id` int(11) NOT NULL auto_increment,
  `wrapper` int(11) NOT NULL,
  `name` varchar(50) NOT NULL,
  `value` longtext NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=10 ;

-- --------------------------------------------------------

--
-- Table structure for table `wrapper_input`
--

DROP TABLE IF EXISTS `wrapper_input`;
CREATE TABLE IF NOT EXISTS `wrapper_input` (
  `id` int(11) NOT NULL auto_increment,
  `description` longtext,
  `wrapper` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `defaultValue` varchar(200) default NULL,
  `flag` varchar(100) NOT NULL,
  `required` tinyint(1) NOT NULL,
  `order` int(11) NOT NULL,
  `type` longtext NOT NULL,
  `displayType` enum('Hidden','Advanced','Default') NOT NULL default 'Default',
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=22758 ;

-- --------------------------------------------------------

--
-- Table structure for table `wrapper_output`
--

DROP TABLE IF EXISTS `wrapper_output`;
CREATE TABLE IF NOT EXISTS `wrapper_output` (
  `id` int(11) NOT NULL auto_increment,
  `wrapper` int(11) NOT NULL,
  `name` varchar(45) NOT NULL,
  `description` longtext,
  `value` varchar(100) default NULL,
  `type` int(11) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=643 ;

-- --------------------------------------------------------

--
-- Table structure for table `wrapper_star`
--

DROP TABLE IF EXISTS `wrapper_star`;
CREATE TABLE IF NOT EXISTS `wrapper_star` (
  `id` int(11) NOT NULL auto_increment,
  `user` int(11) NOT NULL,
  `wrapper` int(11) NOT NULL,
  `rating` int(11) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=28 ;
