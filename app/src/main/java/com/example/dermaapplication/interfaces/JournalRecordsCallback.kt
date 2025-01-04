package com.example.dermaapplication.interfaces

import com.example.dermaapplication.items.JournalRecord

interface JournalRecordsCallback {
    fun onJournalRecordsFetched(journalRecords: List<JournalRecord>)
    fun onError(message: String)
}