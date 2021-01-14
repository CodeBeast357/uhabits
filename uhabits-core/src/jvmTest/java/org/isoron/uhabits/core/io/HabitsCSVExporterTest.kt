/*
 * Copyright (C) 2016-2021 Álinson Santos Xavier <git@axavier.org>
 *
 * This file is part of Loop Habit Tracker.
 *
 * Loop Habit Tracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Loop Habit Tracker is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.isoron.uhabits.core.io

import junit.framework.Assert.assertNotNull
import junit.framework.Assert.assertTrue
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.models.Habit
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.util.LinkedList
import java.util.zip.ZipFile

class HabitsCSVExporterTest : BaseUnitTest() {
    private var baseDir: File? = null
    @Before
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        habitList.add(fixtures.createShortHabit())
        habitList.add(fixtures.createEmptyHabit())
        baseDir = Files.createTempDirectory("csv").toFile()
        assertNotNull(baseDir)
    }

    @Throws(Exception::class)
    override fun tearDown() {
        FileUtils.deleteDirectory(baseDir)
        super.tearDown()
    }

    @Test
    @Throws(IOException::class)
    fun testExportCSV() {
        val selected: MutableList<Habit> = LinkedList()
        for (h in habitList) selected.add(h)
        val exporter = HabitsCSVExporter(
            habitList,
            selected,
            baseDir!!
        )
        val filename = exporter.writeArchive()
        assertAbsolutePathExists(filename)
        val archive = File(filename)
        unzip(archive)
        assertPathExists("Habits.csv")
        assertPathExists("001 Meditate/Checkmarks.csv")
        assertPathExists("001 Meditate/Scores.csv")
        assertPathExists("002 Wake up early")
        assertPathExists("002 Wake up early/Checkmarks.csv")
        assertPathExists("002 Wake up early/Scores.csv")
        assertPathExists("Checkmarks.csv")
        assertPathExists("Scores.csv")
    }

    @Throws(IOException::class)
    private fun unzip(file: File) {
        val zip = ZipFile(file)
        val e = zip.entries()
        while (e.hasMoreElements()) {
            val entry = e.nextElement()
            val stream = zip.getInputStream(entry)
            val outputFilename = String.format(
                "%s/%s",
                baseDir!!.absolutePath,
                entry.name
            )
            val out = File(outputFilename)
            val parent = out.parentFile
            parent?.mkdirs()
            IOUtils.copy(stream, FileOutputStream(out))
        }
        zip.close()
    }

    private fun assertPathExists(s: String) {
        assertAbsolutePathExists(String.format("%s/%s", baseDir!!.absolutePath, s))
    }

    private fun assertAbsolutePathExists(s: String) {
        val file = File(s)
        assertTrue(
            String.format("File %s should exist", file.absolutePath),
            file.exists()
        )
    }
}
