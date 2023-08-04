---
layout: default
title: Tournaments
---

Tournaments is a simple Android app written in Jetpack Compose.

It can be used for keeping track of tournaments containing multiple games played with multiple
players. At the moment, it can be used best with croquet (or similar sports).

I am currently converting the app to
a [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/) app to be able to
also use it on a desktop.

## Installation

The easiest way is to install it from
the [Google Play Store](https://play.google.com/store/apps/details?id=me.frauenfelderflorian.tournamentscompose).

If you want to try the latest, unstable version, clone the GitHub repository and install and run
it [like this](https://developer.android.com/studio/run).

## Usage

When first opening the app, you will see the list of all tournaments (will be empty for now, so you
just see a little hint). To change some settings, tap the settings icon in the top right corner. To
see some information about the app, tap the i. For import and export functionality, tap the three
dots and then your desired option. To add a new tournament, tap the + in the bottom right corner. To
view an existing tournament, tap on its name. To edit an existing tournament, tap the pen on the
right side.

Inside the settings screen, there are two sections: one for general, behavioral settings (App) and
one for some presets for new tournaments.

If you want to add a new tournament or edit an existing one, you will see the "Edit tournament"
screen. Here, you can change the name, start and end date and point system of the tournament. When
creating a new tournament,you need to define the players and you can decide if you want to use the
presets from the settings page for the players and the point system. To create the tournament or
submit the changes, tap the tick in the top right corner. To delete the tournament, tap the bin.

When viewing an existing tournament, you see the list of all games in the tournament and the current
ranking on the second page. To edit the tournament, tap the pen in the top right corner. To export
only this tournament to a file, tap the arrow pointing up. To add a game, tap the + in the bottom
right corner. To view an existing game, tap on its name. To edit an existing game, tap the pen on
the right side. On the second page: to add a player to the tournament, tap the + in the bottom right
corner. To view a player's game history, tap on their name. To edit or delete an existing player,
tap the three dots on the right side.

If you want to add a new game or edit an existing one, you will see the "Edit game" screen. On the
first page, you can change the date, number of total and reached hoops and the difficulty. On the
second page, you can edit the ranking for this game. If you don't select a player, they will be
assumed absent. Spaces between players don't matter. To create the game or submit the changes, tap
the tick in the top right corner. To delete the game, tap the bin.

## How it works

On the Android app, the settings are stored
using [Datastore](https://developer.android.com/topic/libraries/architecture/datastore) and the
tournaments are stored using [Room](https://developer.android.com/training/data-storage/room). When
exporting/importing tournaments, [GSON](https://github.com/google/gson) is used to convert the data
to JSON. For navigation between different
screens, [Decompose](https://github.com/arkivanov/Decompose) is used. Strings are retrieved
using [Moko resources](https://github.com/icerockdev/moko-resources).
[Accompanist](https://github.com/google/accompanist) is used to adjust the color of the system
bars (navigation and status bar).
