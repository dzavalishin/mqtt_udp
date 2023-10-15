package proto

/**
 *
 * MQTT/UDP project
 *
 * https://github.com/dzavalishin/mqtt_udp
 * Copyright (C) 2017-2023 Dmitry Zavalishin, dz@dz.ru
 *
 * @file
 * @brief Topic name match
 *
 *
**/

/**
 *
 * @brief Compare topic name against wildcard or topic name.
 *
 * @param filter     Topic name or wildcard to compare with.
 *
 * @param topicName  Topic name to compare.
 *
 * @return true if topicName matches filter.
 *
**/
func mqtt_udp_match(filter string, topicName string) bool {

	var tc = 0
	var fc = 0

	var tlen = len(topicName)
	var flen = len(filter)

	for {
		// begin of path part

		if filter[fc] == '+' {
			fc++ // eat +
			// matches one path part, skip all up to / or end in topic
			for (tc < tlen) && (topicName[tc] != '/') {
				tc++ // eat all non slash
			}

			// now either both have /, or both at end

			// both finished
			if (tc == tlen) && (fc == flen) {
				return true
			}

			// one finished, other not
			if (tc == tlen) != (fc == flen) {
				return false
			}

			// both continue
			if (topicName[tc] == '/') && (filter[fc] == '/') {
				tc++
				fc++
				continue // path part eaten
			}
			// one of them is not '/' ?
			return false
		}

		// TODO check it to be at end?
		// we came to # in filter, done
		if filter[fc] == '#' {
			return true
		}

		// check parts to be equal

		for {
			// both finished
			if (tc == tlen) && (fc == flen) {
				return true
			}

			// one finished
			if (tc == tlen) || (fc == flen) {
				return false
			}

			// both continue
			if (topicName[tc] == '/') && (filter[fc] == '/') {
				tc++
				fc++
				break // path part eaten
			}
			// both continue

			if topicName[tc] != filter[fc] {
				return false
			}

			// continue
			tc++
			fc++
		}

	}

}
