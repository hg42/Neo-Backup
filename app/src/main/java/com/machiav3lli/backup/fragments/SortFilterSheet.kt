/*
 * OAndBackupX: open-source apps backup and restore app.
 * Copyright (C) 2020  Antonios Hazim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.machiav3lli.backup.fragments

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.ChipGroup
import com.machiav3lli.backup.Constants
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.databinding.SheetSortFilterBinding
import com.machiav3lli.backup.handler.SortFilterManager.getFilterPreferences
import com.machiav3lli.backup.handler.SortFilterManager.saveFilterPreferences
import com.machiav3lli.backup.items.SortFilterModel
import com.machiav3lli.backup.utils.getDefaultSharedPreferences

class SortFilterSheet(private var sortFilterModel: SortFilterModel = SortFilterModel()) : BottomSheetDialogFragment() {
    private lateinit var binding: SheetSortFilterBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val sheet = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        sheet.setOnShowListener { d: DialogInterface ->
            val bottomSheetDialog = d as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<FrameLayout>(R.id.design_bottom_sheet)
            if (bottomSheet != null) BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
        }
        return sheet
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = SheetSortFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupOnClicks()
        setupChips()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        sortFilterModel = getFilterPreferences(requireContext())
    }

    private fun setupOnClicks() {
        binding.dismiss.setOnClickListener { dismissAllowingStateLoss() }
        binding.reset.setOnClickListener {
            saveFilterPreferences(requireContext(), SortFilterModel("0000"))
            requireMainActivity().refreshView()
            dismissAllowingStateLoss()
        }
        binding.apply.setOnClickListener {
            saveFilterPreferences(requireContext(), sortFilterModel)
            requireMainActivity().refreshView()
            dismissAllowingStateLoss()
        }
    }

    private fun setupChips() {
        binding.sortBy.check(sortFilterModel.sortById)
        binding.sortBy.setOnCheckedChangeListener { _: ChipGroup?, checkedId: Int -> sortFilterModel.putSortBy(checkedId) }
        binding.filters.check(sortFilterModel.filterId)
        binding.filters.setOnCheckedChangeListener { _: ChipGroup?, checkedId: Int -> sortFilterModel.putFilter(checkedId) }
        binding.backupFilters.check(sortFilterModel.backupFilterId)
        binding.backupFilters.setOnCheckedChangeListener { _: ChipGroup?, checkedId: Int -> sortFilterModel.putBackupFilter(checkedId) }
        binding.specialFilters.check(sortFilterModel.specialFilterId)
        binding.specialFilters.setOnCheckedChangeListener { _: ChipGroup?, checkedId: Int -> sortFilterModel.putSpecialFilter(checkedId) }
        if (getDefaultSharedPreferences(requireContext()).getBoolean(Constants.PREFS_ENABLESPECIALBACKUPS, false)) {
            binding.showOnlySpecial.visibility = View.VISIBLE
        } else {
            binding.showOnlySpecial.visibility = View.GONE
        }
    }

    private fun requireMainActivity(): MainActivityX = MainActivityX.act!! //super.requireActivity() as MainActivityX
}