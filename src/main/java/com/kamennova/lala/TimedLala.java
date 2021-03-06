package com.kamennova.lala;

import com.kamennova.lala.common.Note;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TimedLala {
    public static void main(String[] args) {
        // goose
        List<Note> melody1 = List.of(
                new Note(5, 4),
                new Note(4, 4),
                new Note(2, 4),
                new Note(0, 4),

                new Note(7, 8),
                new Note(7, 8));


        List<Note> melody2 = List.of(
                new Note(5, 4),
                new Note(4, 4),
                new Note(2, 4),
                new Note(0, 4),

                new Note(7, 8),
                new Note(7, 8));

    }
    private class NoteSeq{
        public List<Note> notes;

        NoteSeq(List<Note> notes) {
            this.notes = notes;
        }

        public int getDuration(){
            return this.notes.stream().map(n -> (int) n.duration).reduce(Integer::sum).get();
        }

        @Override
        public int hashCode() {
            return this.getDuration();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final NoteSeq other = (NoteSeq) obj;

            return true; // todo
        }
    }

    private List<List<NoteSeq>> getTimeSequences(List<Note> notes, int totalDuration, int batchDuration) {
        int startsNum = totalDuration / batchDuration;

        List<List<NoteSeq>> sequences = new ArrayList<>();

        for (int i = 0; i < startsNum; i++) {
            List<NoteSeq> curr = timeBatches(notes.subList(i, notes.size()), batchDuration);
            System.out.println(curr.get(0));
            sequences.add(curr);
        }

        return sequences;
    }


    private List<NoteSeq> timeBatches(List<Note> source, int duration) {
        List<NoteSeq> res = new ArrayList<>();
        List<Note> currSeq = new ArrayList<>();
        int currDuration = 0;

        for (Note currNote : source) {
            int durationLeft = duration - currDuration;
            if (currNote.duration >= durationLeft) {
                currSeq.add(new Note(currNote.interval, durationLeft));
                res.add(new NoteSeq(currSeq));
                currSeq = new ArrayList<>();
                currDuration = 0;
            } else {
                currSeq.add(currNote);
                currDuration += currNote.duration;
            }
        }

        return res;
    }

    private HashMap<NoteSeq, Integer> timedStore3 = new HashMap<>();
}
