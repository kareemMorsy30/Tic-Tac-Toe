
SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";

--
-- Database: `tictactoe`
--

-- --------------------------------------------------------

--
-- Table structure for table `games`
--

CREATE DATABASE IF NOT EXISTS tictactoe;
USE tictactoe;

CREATE TABLE `games` (
  `id` int(11) NOT NULL,
  `xplayer` int(11) NOT NULL,
  `oplayer` int(11) NOT NULL,
  `data` varchar(180) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `games`
--

INSERT INTO `games` (`id`, `xplayer`, `oplayer`, `data`) VALUES
(1, 7, 5, '{\"type\": \"savedGame\", \"xplayer\": 7, \"oplayer\": 5, \"cells\":[\"\",\"O\",\"X\",\"\",\"X\",\"X\",\"O\",\"\",\"O\"], \"isTurnx\": true}');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `email` varchar(30) NOT NULL,
  `username` varchar(40) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `password` varchar(30) NOT NULL,
  `points` int(11) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `email`, `username`, `password`, `points`) VALUES
(1, 'kareemmorsy30@gmail.com', 'Kareem', '123456', 10),
(5, 'ahmedmorsy50@gmail.com', 'Ahmed', '123456', 47),
(6, 'kdsnvskvnsv@sdvsv.com', 'asdasd', 'asdasd', 0),
(7, 'Moaz', 'moaz', '123', 117);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `games`
--
ALTER TABLE `games`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `games`
--
ALTER TABLE `games`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

